package com.qrorder.repo;

import com.qrorder.model.PaymentStatus;
import com.qrorder.model.Session;
import com.qrorder.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionToken(String sessionToken);

    // All open (non-closed) sessions at a table - since several devices can
    // each hold their own independent bill at the same table.
    List<Session> findByTableIdAndStatusNot(Long tableId, SessionStatus status);

    List<Session> findByStatusNot(SessionStatus status);

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByStatusNotAndPaymentStatusNotAndLastActivityAtBefore(
            SessionStatus status, PaymentStatus paymentStatus, Instant cutoff);

    List<Session> findByStatusNotAndCustomerPhone(SessionStatus status, String customerPhone);
}
