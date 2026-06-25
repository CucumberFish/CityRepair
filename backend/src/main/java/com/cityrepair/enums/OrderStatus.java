package com.cityrepair.enums;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING_REVIEW,
    PENDING_ASSIGN,
    PENDING_ACCEPT,
    PROCESSING,
    COMPLETED,
    EVALUATED,
    REJECTED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING_REVIEW, Set.of(PENDING_ASSIGN, REJECTED, CANCELLED),
            PENDING_ASSIGN, Set.of(PENDING_ACCEPT),
            PENDING_ACCEPT, Set.of(PROCESSING),
            PROCESSING, Set.of(COMPLETED),
            COMPLETED, Set.of(EVALUATED)
    );

    public boolean canMoveTo(OrderStatus next) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }
}
