package com.qrorder.model;

public enum OrderStatus {
    PLACED,     // "Order received" in the diagram - waiting for chef review
    ACCEPTED,   // chef accepted -> "Prepare food"
    REJECTED,   // chef rejected -> customer notified, can Reorder
    READY,      // food cooked, waiting for waiter to deliver
    SERVED      // waiter delivered - "Mark served"
}
