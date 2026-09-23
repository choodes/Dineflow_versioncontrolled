package com.qrorder.model;

public enum WaiterCallType {
    CUSTOMER_CALL,     // customer tapped "Call Waiter" (for cash/card requests, help, etc.)
    PAYMENT_COLLECT,   // customer is ready to pay - waiter must attend, collect/confirm payment
    REGULAR_CHECK      // automatic: pay-later session idle 20+ min, waiter should politely check in
}
