package com.campuscomplaint.dto;

import jakarta.validation.constraints.NotNull;
import com.campuscomplaint.model.ComplaintStatus;

/**
 * DTO for updating the status of a complaint.
 */
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    // ----- Getters and Setters -----

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }
}
