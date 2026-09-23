package com.qrorder.service;

import com.qrorder.model.PaymentStatus;
import com.qrorder.model.Session;
import com.qrorder.model.SessionStatus;
import com.qrorder.model.WaiterCallType;
import com.qrorder.repo.SessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Any open session with no new order in the last 25 minutes, and not yet
 * paid, is flagged for the waiter to check on - covers both "customer
 * scanned but never ordered" and "customer ordered once, went quiet"
 * equally, regardless of whether they picked Pay Now or Pay Later.
 */
@Component
public class RegularCheckScheduler {

    public static final int IDLE_THRESHOLD_MINUTES = 25;

    private final SessionRepository sessionRepository;
    private final WaiterCallService waiterCallService;

    public RegularCheckScheduler(SessionRepository sessionRepository, WaiterCallService waiterCallService) {
        this.sessionRepository = sessionRepository;
        this.waiterCallService = waiterCallService;
    }

    @Scheduled(fixedRate = 60_000) // every 60 seconds
    public void checkIdleSessions() {
        Instant cutoff = Instant.now().minus(IDLE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        List<Session> idle = sessionRepository.findByStatusNotAndPaymentStatusNotAndLastActivityAtBefore(
                SessionStatus.CLOSED, PaymentStatus.PAID, cutoff);

        for (Session s : idle) {
            if (!waiterCallService.hasOpenRegularCheck(s.getId())) {
                waiterCallService.raise(s.getTableId(), s.getId(), WaiterCallType.REGULAR_CHECK);
            }
        }
    }
}
