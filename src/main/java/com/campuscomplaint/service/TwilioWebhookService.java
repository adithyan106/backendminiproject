package com.campuscomplaint.service;

import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.repository.ComplaintRepository;
import com.twilio.security.RequestValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TwilioWebhookService {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(START|DONE)\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);

    private final ComplaintRepository complaintRepository;
    private final TwilioSmsService twilioSmsService;

    @Value("${twilio.validate-signature:false}")
    private boolean validateSignature;

    @Value("${twilio.webhook-url:}")
    private String webhookUrl;

    @Value("${twilio.auth-token:}")
    private String authToken;

    public TwilioWebhookService(ComplaintRepository complaintRepository,
                                TwilioSmsService twilioSmsService) {
        this.complaintRepository = complaintRepository;
        this.twilioSmsService = twilioSmsService;
    }

    public boolean isValidRequest(String signature, Map<String, String> formParams) {
        if (!validateSignature) {
            return true;
        }

        if (signature == null || signature.isBlank()) {
            return false;
        }

        if (webhookUrl == null || webhookUrl.isBlank() || authToken == null || authToken.isBlank()) {
            return false;
        }

        RequestValidator validator = new RequestValidator(authToken);
        return validator.validate(webhookUrl, formParams, signature);
    }

    public String processIncomingMessage(String fromPhoneNumber, String body) {
        if (body == null || body.isBlank()) {
            return usageMessage();
        }

        String normalizedBody = body.trim().replaceAll("\\s+", " ");
        Matcher matcher = COMMAND_PATTERN.matcher(normalizedBody);
        if (!matcher.matches()) {
            return usageMessage();
        }

        String command = matcher.group(1).toUpperCase();
        Long complaintId;
        try {
            complaintId = Long.parseLong(matcher.group(2));
        } catch (NumberFormatException ex) {
            return "Invalid complaint ID. Use START <id> or DONE <id>.";
        }

        Optional<Complaint> optionalComplaint = complaintRepository.findById(complaintId);
        if (optionalComplaint.isEmpty()) {
            return "Complaint ID " + complaintId + " was not found.";
        }

        Complaint complaint = optionalComplaint.get();
        if (!isAssignedTechnician(complaint.getAssignedToContact(), fromPhoneNumber)) {
            return "You are not authorized to update complaint ID " + complaintId + ".";
        }

        if ("START".equals(command)) {
            return handleStartCommand(complaint);
        }

        if ("DONE".equals(command)) {
            return handleDoneCommand(complaint);
        }

        return usageMessage();
    }

    private String handleStartCommand(Complaint complaint) {
        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            return "Complaint ID " + complaint.getId() + " is already RESOLVED.";
        }

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
            complaintRepository.save(complaint);
        }

        twilioSmsService.sendAdminNotification(
                "Technician started work | Complaint ID: " + complaint.getId()
                        + " | Title: " + complaint.getTitle()
                        + " | Current Status: IN_PROGRESS",
                complaint.getId()
        );

        return "Updated complaint ID " + complaint.getId() + " to IN_PROGRESS.";
    }

    private String handleDoneCommand(Complaint complaint) {
        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            return "Complaint ID " + complaint.getId() + " is already RESOLVED by admin.";
        }

        if (complaint.getStatus() == ComplaintStatus.PENDING) {
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
            complaintRepository.save(complaint);
        }

        twilioSmsService.sendAdminNotification(
                "WORK DONE reported by technician | Complaint ID: " + complaint.getId()
                        + " | Title: " + complaint.getTitle()
                        + " | Please verify and mark RESOLVED in admin dashboard.",
                complaint.getId()
        );

        return "Work-done update received for complaint ID " + complaint.getId() + ". Admin will verify and mark RESOLVED.";
    }

    private boolean isAssignedTechnician(String assignedContact, String fromPhoneNumber) {
        String assignedNormalized = normalizePhoneNumber(assignedContact);
        String fromNormalized = normalizePhoneNumber(fromPhoneNumber);

        if (assignedNormalized.isBlank() || fromNormalized.isBlank()) {
            return false;
        }

        if (assignedNormalized.equals(fromNormalized)) {
            return true;
        }

        String assignedLast10 = last10Digits(assignedNormalized);
        String fromLast10 = last10Digits(fromNormalized);
        return !assignedLast10.isBlank() && assignedLast10.equals(fromLast10);
    }

    private String normalizePhoneNumber(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }

    private String last10Digits(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.length() <= 10 ? value : value.substring(value.length() - 10);
    }

    private String usageMessage() {
        return "Invalid command. Use START <id> or DONE <id>.";
    }
}
