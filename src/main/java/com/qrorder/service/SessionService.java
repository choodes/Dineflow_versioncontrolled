package com.qrorder.service;

import com.qrorder.model.*;
import com.qrorder.repo.SessionRepository;
import com.qrorder.repo.TableRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TableRepository tableRepository;

    public SessionService(SessionRepository sessionRepository, TableRepository tableRepository) {
        this.sessionRepository = sessionRepository;
        this.tableRepository = tableRepository;
    }

    public RestaurantTable resolveTable(String qrToken) {
        return tableRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalArgumentException("Unknown QR code"));
    }

    /**
     * Core of the "different devices scanning the same QR" rule: if the
     * device already holds a valid, still-open session token, resume it.
     * Otherwise ALWAYS create a brand new session for this device, even if
     * other sessions are already open at the same table - each device gets
     * its own independent bill.
     */
    public Session getOrCreateSession(Long tableId, String existingToken) {
        if (existingToken != null && !existingToken.isBlank()) {
            var existing = sessionRepository.findBySessionToken(existingToken).orElse(null);
            if (existing != null && existing.getTableId().equals(tableId)
                    && existing.getStatus() != SessionStatus.CLOSED) {
                return existing;
            }
            // token invalid / closed / belongs to a different table -> fall through and create new
        }
        Session session = new Session();
        session.setTableId(tableId);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setStatus(SessionStatus.ACTIVE);
        return sessionRepository.save(session);
    }

    public Session getOpenSessionByToken(String token) {
        Session s = sessionRepository.findBySessionToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (s.getStatus() == SessionStatus.CLOSED) {
            throw new IllegalStateException("Session is closed. Please rescan the QR code.");
        }
        return s;
    }

    public Session getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }

    public void touchActivity(Session session) {
        session.setLastActivityAt(Instant.now());
        sessionRepository.save(session);
    }

    /** "Pay now?" decision point in the workflow. */
    public Session choosePaymentTiming(String token, PaymentChoice choice) {
        Session s = getOpenSessionByToken(token);
        s.setPaymentChoice(choice);
        return sessionRepository.save(s);
    }

    /** "Close order?" = YES -> customer wants the bill. */
    public Session requestBill(String token) {
        Session s = getOpenSessionByToken(token);
        s.setStatus(SessionStatus.BILL_REQUESTED);
        return sessionRepository.save(s);
    }

    public Session save(Session s) {
        return sessionRepository.save(s);
    }

    public List<Session> findOpenSessionsForTable(Long tableId) {
        return sessionRepository.findByTableIdAndStatusNot(tableId, SessionStatus.CLOSED);
    }

    public List<Session> findAllOpenSessions() {
        return sessionRepository.findByStatusNot(SessionStatus.CLOSED);
    }

    /** "Confirm payment" -> finalized -> Leave. */
    public Session closeSession(Long sessionId) {
        Session s = getById(sessionId);
        s.setStatus(SessionStatus.CLOSED);
        s.setPaymentStatus(PaymentStatus.PAID);
        s.setClosedAt(Instant.now());
        return sessionRepository.save(s);
    }

    /** Manager "Override/close sessions" branch - e.g. walkout write-off, comped meal. */
    public Session managerOverrideClose(Long sessionId, String reason) {
        Session s = getById(sessionId);
        s.setStatus(SessionStatus.CLOSED);
        s.setManagerOverride(true);
        s.setOverrideReason(reason);
        s.setClosedAt(Instant.now());
        return sessionRepository.save(s);
    }

    /** Captured right after scan, before the customer can browse the menu. */
    public Session setCustomerDetails(String token, String phone, Integer partySize) {
        if (phone == null || !phone.matches("[6-9][0-9]{9}")) {
            throw new IllegalArgumentException("Enter a valid 10-digit mobile number");
        }
        if (partySize == null || partySize < 1 || partySize > 30) {
            throw new IllegalArgumentException("Enter a valid number of members (1-30)");
        }
        Session s = getOpenSessionByToken(token);
        s.setCustomerPhone(phone);
        s.setPartySize(partySize);
        return sessionRepository.save(s);
    }

    /** Counter staff looking up a bill by the phone number the customer gave at the table. */
    public List<Session> findOpenSessionsByPhone(String phone) {
        return sessionRepository.findByStatusNotAndCustomerPhone(SessionStatus.CLOSED, phone);
    }

    public SessionRepository repo() {
        return sessionRepository;
    }
}
