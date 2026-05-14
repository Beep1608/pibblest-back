package com.nss.pibblest.shared.enums;

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

    @JsonValue // Permite que Spring reciba y envíe el texto en minúsculas en el JSON
    public String getValue() {
        return value;
    }
}