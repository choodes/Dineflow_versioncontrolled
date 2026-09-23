package com.qrorder.model;

public enum PaymentStatus {
    UNPAID,
    PROCESSING, // UPI payment initiated, waiting for gateway confirmation
    PAID
}
