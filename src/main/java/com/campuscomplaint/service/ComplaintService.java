package com.campuscomplaint.service;

import com.campuscomplaint.dto.ComplaintRequest;
import com.campuscomplaint.dto.RepairAssignee;
import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for handling complaint business logic.
 * Includes CRUD operations and automatic escalation of stale complaints.
 */
@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintRoutingService complaintRoutingService;
    private final TwilioSmsService twilioSmsService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintRoutingService complaintRoutingService,
                            TwilioSmsService twilioSmsService) {
        this.complaintRepository = complaintRepository;
        this.complaintRoutingService = complaintRoutingService;
        this.twilioSmsService = twilioSmsService;
    }

    /**
     * Create a new complaint from the submitted form data.
     */
    public Complaint createComplaint(ComplaintRequest request) {
        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());
        complaint.setLocation(request.getLocation());
        complaint.setPriority(resolvePriority(request.getPriority()));
        complaint.setStatus(ComplaintStatus.PENDING);

        MultipartFile image = request.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFileName = image.getOriginalFilename();
                String fileExtension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                
                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Assuming the server uses /uploads/ mapped to the local uploads directory
                complaint.setImageUrl("/uploads/" + newFileName);
            } catch (Exception e) {
                System.err.println("Could not store image " + e.getMessage());
            }
        }

        RepairAssignee assignee = complaintRoutingService.getAssigneeForCategory(request.getCategory());
        complaint.setAssignedToName(assignee.getName());
        complaint.setAssignedToContact(assignee.getContact());

        Complaint savedComplaint = complaintRepository.save(complaint);
        twilioSmsService.sendComplaintCreatedSms(savedComplaint, assignee.getContact());

        return savedComplaint;
    }

    private String resolvePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "MEDIUM";
        }

        String normalized = priority.trim().toUpperCase();
        return switch (normalized) {
            case "LOW", "MEDIUM", "HIGH", "CRITICAL" -> normalized;
            default -> "MEDIUM";
        };
    }

    /**
     * Retrieve all complaints, ordered by most recent first.
     */
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    /**
     * Retrieve a single complaint by its ID.
     * Throws RuntimeException if not found.
     */
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));
    }

    /**
     * Update the status of an existing complaint.
     */
    public Complaint updateComplaintStatus(Long id, ComplaintStatus status) {
        Complaint complaint = getComplaintById(id);
        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(status);
        Complaint savedComplaint = complaintRepository.save(complaint);
        twilioSmsService.sendComplaintStatusUpdatedSms(savedComplaint, previousStatus, status);
        return savedComplaint;
    }

    /**
     * Delete a complaint by its ID.
     */
    public void deleteComplaint(Long id) {
        if (!complaintRepository.existsById(id)) {
            throw new RuntimeException("Complaint not found with id: " + id);
        }
        complaintRepository.deleteById(id);
    }

    /**
     * Escalation Logic:
     * Runs every hour (3600000 ms). Finds all complaints that are still PENDING
     * and were created more than 48 hours ago, then changes their status to ESCALATED.
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void escalatePendingComplaints() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        List<Complaint> staleComplaints = complaintRepository
                .findByStatusAndCreatedAtBefore(ComplaintStatus.PENDING, cutoff);

        for (Complaint complaint : staleComplaints) {
            ComplaintStatus previousStatus = complaint.getStatus();
            complaint.setStatus(ComplaintStatus.ESCALATED);
            Complaint savedComplaint = complaintRepository.save(complaint);
            twilioSmsService.sendComplaintStatusUpdatedSms(savedComplaint, previousStatus, ComplaintStatus.ESCALATED);
            System.out.println("Escalated complaint ID: " + complaint.getId()
                    + " - " + complaint.getTitle());
        }
    }
}
