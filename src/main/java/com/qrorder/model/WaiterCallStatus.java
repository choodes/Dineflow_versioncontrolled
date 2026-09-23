package com.qrorder.model;

public enum WaiterCallStatus {
    OPEN,       // "Waiter notified"
    ATTENDING,  // "Attend table" - waiter is on the way / at the table
    RESOLVED    // "Confirm payment" / call handled
}
