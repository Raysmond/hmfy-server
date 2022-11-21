package com.shield.domain.enumeration;

public enum PlanStatus {
    WAIT_SHIP(1),

    CANCELED(2),

    SHIPPED(3),

    EXPIRED(4);

    private final int status;

    PlanStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
