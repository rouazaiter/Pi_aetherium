package com.education.platform.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {

    USER("user"),
    ADMIN("admin");

    private final String tableValue;

    Role(String tableValue) {
        this.tableValue = tableValue;
    }

    @JsonValue
    public String getTableValue() {
        return tableValue;
    }

    @JsonCreator
    public static Role fromTableValue(String value) {
        if (value == null) {
            return null;
        }
        for (Role r : values()) {
            if (r.tableValue.equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rôle inconnu: " + value);
    }
}
