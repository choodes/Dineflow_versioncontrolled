package com.qrorder.service;

import com.qrorder.model.*;
import com.qrorder.repo.WaiterCallRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WaiterCallService {

    private final WaiterCallRepository waiterCallRepository;

    public WaiterCallService(WaiterCallRepository waiterCallRepository) {
        this.waiterCallRepository = waiterCallRepository;
    }

    public WaiterCall raise(Long tableId, Long sessionId, WaiterCallType type) {
        WaiterCall call = new WaiterCall();
        call.setTableId(tableId);
        call.setSessionId(sessionId);
        call.setType(type);
        call.setStatus(WaiterCallStatus.OPEN);
        return waiterCallRepository.save(call);
    }

    public List<WaiterCall> listOpen() {
        return waiterCallRepository.findByStatusNotOrderByCreatedAtAsc(WaiterCallStatus.RESOLVED);
    }

    /** "Attend table" */
    public WaiterCall attend(Long callId) {
        WaiterCall call = get(callId);
        call.setStatus(WaiterCallStatus.ATTENDING);
        return waiterCallRepository.save(call);
    }

    /** "Confirm payment" / call handled */
    public WaiterCall resolve(Long callId) {
        WaiterCall call = get(callId);
        call.setStatus(WaiterCallStatus.RESOLVED);
        call.setResolvedAt(Instant.now());
        return waiterCallRepository.save(call);
    }

    public WaiterCall get(Long callId) {
        return waiterCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Waiter call not found"));
    }

    public boolean hasOpenRegularCheck(Long sessionId) {
        return waiterCallRepository.findBySessionIdAndStatusNot(sessionId, WaiterCallStatus.RESOLVED)
                .stream().anyMatch(c -> c.getType() == WaiterCallType.REGULAR_CHECK);
    }
}
