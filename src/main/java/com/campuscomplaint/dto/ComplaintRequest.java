package com.campuscomplaint.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new complaint.
 * Validates that required fields are not blank.
 */
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Location is required")
    private String location;

    private String priority;

    private org.springframework.web.multipart.MultipartFile image;

    // ----- Getters and Setters -----

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

    public org.springframework.web.multipart.MultipartFile getImage() {
        return image;
    }

    public void setImage(org.springframework.web.multipart.MultipartFile image) {
        this.image = image;
    }
}
