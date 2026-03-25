package com.campuscomplaint.dto;

public class RepairAssignee {
    private final String name;
    private final String contact;

    public RepairAssignee(String name, String contact) {
        this.name = name;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }
}
