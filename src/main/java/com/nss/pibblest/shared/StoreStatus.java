package com.nss.pibblest.shared;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StoreStatus {
    ACTIVE("active"),
    CLOSED("closed"),
    MAINTENANCE("maintenance"),
    PRE_ACTIVE("pre-active");

    private final String value;

    StoreStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
