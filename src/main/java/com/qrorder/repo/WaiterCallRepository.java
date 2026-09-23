package com.qrorder.repo;

import com.qrorder.model.WaiterCall;
import com.qrorder.model.WaiterCallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WaiterCallRepository extends JpaRepository<WaiterCall, Long> {
    List<WaiterCall> findByStatusNotOrderByCreatedAtAsc(WaiterCallStatus status);
    List<WaiterCall> findBySessionIdAndStatusNot(Long sessionId, WaiterCallStatus status);
}
