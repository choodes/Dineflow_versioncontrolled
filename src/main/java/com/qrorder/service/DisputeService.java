package com.qrorder.service;

import com.qrorder.model.Dispute;
import com.qrorder.model.DisputeStatus;
import com.qrorder.repo.DisputeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public Dispute raise(Long sessionId, String note, String raisedBy) {
        Dispute d = new Dispute();
        d.setSessionId(sessionId);
        d.setNote(note);
        d.setRaisedBy(raisedBy);
        d.setStatus(DisputeStatus.OPEN);
        return disputeRepository.save(d);
    }

    public List<Dispute> listOpen() {
        return disputeRepository.findByStatus(DisputeStatus.OPEN);
    }

    public List<Dispute> listAll() {
        return disputeRepository.findAll();
    }

    public Dispute resolve(Long id, String resolutionNote) {
        Dispute d = disputeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
        d.setStatus(DisputeStatus.RESOLVED);
        d.setResolutionNote(resolutionNote);
        d.setResolvedAt(Instant.now());
        return disputeRepository.save(d);
    }
}
