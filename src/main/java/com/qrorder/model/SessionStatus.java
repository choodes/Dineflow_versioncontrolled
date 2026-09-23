package com.qrorder.model;

public enum SessionStatus {
    ACTIVE,          // customer is browsing/ordering
    BILL_REQUESTED,  // "Close order?" = YES in the workflow diagram; customer wants to pay
    CLOSED           // payment confirmed (or manager override) - table/device session finished
}
