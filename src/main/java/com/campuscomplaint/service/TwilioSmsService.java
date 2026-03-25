package com.campuscomplaint.service;

import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    @Value("${twilio.admin-phone-number:}")
    private String adminPhoneNumber;

    @PostConstruct
    public void initializeTwilioClient() {
        if (!twilioEnabled) {
            System.out.println("Twilio SMS disabled (twilio.enabled=false).");
            return;
        }

        if (accountSid == null || accountSid.isBlank()
                || authToken == null || authToken.isBlank()
                || fromPhoneNumber == null || fromPhoneNumber.isBlank()) {
            System.err.println("Twilio SMS enabled but credentials are missing in application.properties");
            return;
        }

        Twilio.init(accountSid, authToken);
        System.out.println("Twilio SMS initialized.");
    }

    public void sendComplaintCreatedSms(Complaint complaint, String toPhoneNumber) {
        if (!twilioEnabled) {
            return;
        }

        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            System.err.println("Skipping Twilio SMS: assignee contact is empty for complaint ID " + complaint.getId());
            return;
        }

        String messageBody = "New Campus Complaint | ID: " + complaint.getId()
                + " | Title: " + complaint.getTitle()
                + " | Category: " + complaint.getCategory()
                + " | Location: " + complaint.getLocation()
                + " | Priority: " + complaint.getPriority()
            + " | Reply START " + complaint.getId() + " when work begins."
            + " Reply DONE " + complaint.getId() + " when work is completed.";

        sendSms(toPhoneNumber, messageBody, complaint.getId());
    }

    public void sendComplaintStatusUpdatedSms(Complaint complaint, ComplaintStatus previousStatus, ComplaintStatus newStatus) {
        if (!twilioEnabled) {
            return;
        }

        String messageBody = "Complaint Status Update | ID: " + complaint.getId()
                + " | Title: " + complaint.getTitle()
                + " | Category: " + complaint.getCategory()
                + " | Status: " + previousStatus + " -> " + newStatus;

        sendSms(complaint.getAssignedToContact(), messageBody, complaint.getId());
        if (adminPhoneNumber != null && !adminPhoneNumber.isBlank()) {
            sendSms(adminPhoneNumber, messageBody, complaint.getId());
        }
    }

    public void sendAdminNotification(String message, Long complaintId) {
        if (!twilioEnabled) {
            return;
        }

        if (adminPhoneNumber == null || adminPhoneNumber.isBlank()) {
            System.err.println("Skipping admin Twilio SMS: admin phone number is not configured.");
            return;
        }

        sendSms(adminPhoneNumber, message, complaintId);
    }

    private void sendSms(String toPhoneNumber, String messageBody, Long complaintId) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            System.err.println("Skipping Twilio SMS: destination phone is empty for complaint ID " + complaintId);
            return;
        }

        try {
            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();
        } catch (Exception ex) {
            System.err.println("Failed to send Twilio SMS for complaint ID " + complaintId + ": " + ex.getMessage());
        }
    }
}
