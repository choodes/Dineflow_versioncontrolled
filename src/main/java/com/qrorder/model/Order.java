package com.qrorder.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Set by the chef when rejecting, shown to the customer next to the
    // "Reorder" button in the workflow.
    private String rejectionReason;

    // If this order was created by tapping "Reorder" after a rejection, this
    // points back at the order it replaces (for traceability only).
    private Long reorderOfOrderId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getReorderOfOrderId() { return reorderOfOrderId; }
    public void setReorderOfOrderId(Long reorderOfOrderId) { this.reorderOfOrderId = reorderOfOrderId; }
}
