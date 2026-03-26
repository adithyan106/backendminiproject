package com.campuscomplaint.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    STUDENT,
    ADMIN;

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Role.valueOf(value.trim().toUpperCase());
    }
}
