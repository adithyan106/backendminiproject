package com.campuscomplaint.controller;

import com.campuscomplaint.dto.ComplaintRequest;
import com.campuscomplaint.dto.StatusUpdateRequest;
import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing campus infrastructure complaints.
 * Exposes endpoints for CRUD operations and status updates.
 */
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * POST /api/complaints
     * Submit a new complaint (supports multipart form data for image upload).
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Complaint> createComplaint(@ModelAttribute ComplaintRequest request) {
        Complaint created = complaintService.createComplaint(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /api/complaints
     * Retrieve all complaints.
     */
    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        List<Complaint> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    /**
     * GET /api/complaints/{id}
     * Retrieve a specific complaint by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
        Complaint complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(complaint);
    }

    /**
     * PUT /api/complaints/{id}/status
     * Update the status of a complaint.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Complaint> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        Complaint updated = complaintService.updateComplaintStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/complaints/{id}
     * Delete a complaint by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.noContent().build();
    }
}
