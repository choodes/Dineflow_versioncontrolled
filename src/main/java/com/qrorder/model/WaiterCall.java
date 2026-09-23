package com.qrorder.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "waiter_calls")
public class WaiterCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tableId;

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaiterCallType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaiterCallStatus status = WaiterCallStatus.OPEN;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public WaiterCallType getType() { return type; }
    public void setType(WaiterCallType type) { this.type = type; }
    public WaiterCallStatus getStatus() { return status; }
    public void setStatus(WaiterCallStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
