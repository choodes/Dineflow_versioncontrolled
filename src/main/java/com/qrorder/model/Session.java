package com.qrorder.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A Session represents ONE bill. Per the workflow: the same table QR can be
 * scanned by several different customer devices at once, and EACH device gets
 * its OWN independent session/bill (this is deliberately different from a
 * single shared per-table tab).
 *
 * A device keeps its session by holding onto {@link #sessionToken} in
 * localStorage; rescanning the same QR on the same device (token still valid)
 * resumes that session instead of creating a new one.
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tableId;

    @Column(nullable = false, unique = true)
    private String sessionToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentChoice paymentChoice = PaymentChoice.UNDECIDED;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Updated whenever an order is placed on this session. Used by the
    // scheduled "regular check" job: a pay-later session with no order
    // activity for 20+ minutes gets a polite waiter check-in, per the
    // workflow's sticky note.
    @Column(nullable = false)
    private Instant lastActivityAt = Instant.now();

    // Captured right after the QR scan, before the customer can browse the
    // menu - lightweight identity signal for staff and for pay-later risk
    // mitigation. No OTP/SMS gateway in this demo, just format validation.
    private String customerPhone;

    // Party size, also captured at the same gate - lets staff and the
    // counter know how many are on the bill.
    private Integer partySize;

    private Instant closedAt;

    // Set true when a manager force-closes a session (e.g. walkout write-off,
    // comped meal) instead of it going through the normal payment flow.
    @Column(nullable = false)
    private boolean managerOverride = false;

    private String overrideReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public PaymentChoice getPaymentChoice() { return paymentChoice; }
    public void setPaymentChoice(PaymentChoice paymentChoice) { this.paymentChoice = paymentChoice; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }
    public boolean isManagerOverride() { return managerOverride; }
    public void setManagerOverride(boolean managerOverride) { this.managerOverride = managerOverride; }
    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
}
