package com.campuscomplaint.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a campus infrastructure complaint.
 * Maps to the 'complaints' table in the database.
 */
@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short title describing the complaint
    @Column(nullable = false)
    private String title;

    // Detailed description of the issue
    @Column(nullable = false, length = 1000)
    private String description;

    // Category: e.g., Electrical, Plumbing, Furniture, Cleanliness, etc.
    @Column(nullable = false)
    private String category;

    // Location on campus where the issue was found
    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 20)
    private String priority;

    // Complaint status: PENDING, IN_PROGRESS, RESOLVED, ESCALATED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    // Timestamp when the complaint was created
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when the complaint was last updated
    private LocalDateTime updatedAt;

    // URL or path to the uploaded image
    @Column(length = 500)
    private String imageUrl;

    @Column(length = 150)
    private String assignedToName;

    @Column(length = 200)
    private String assignedToContact;

    /**
     * Automatically set createdAt before persisting.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically update updatedAt before merging.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ----- Getters and Setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getAssignedToContact() {
        return assignedToContact;
    }

    public void setAssignedToContact(String assignedToContact) {
        this.assignedToContact = assignedToContact;
    }
}
