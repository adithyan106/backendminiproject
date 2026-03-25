package com.campuscomplaint.model;

/**
 * Enum representing the possible statuses of a complaint.
 */
public enum ComplaintStatus {
    PENDING,        // Newly submitted, not yet reviewed
    IN_PROGRESS,    // Being worked on by maintenance
    RESOLVED,       // Issue has been fixed
    ESCALATED       // Auto-escalated after 48 hours without resolution
}
