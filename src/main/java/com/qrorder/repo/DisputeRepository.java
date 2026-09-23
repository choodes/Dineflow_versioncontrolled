package com.qrorder.repo;

import com.qrorder.model.Dispute;
import com.qrorder.model.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByStatus(DisputeStatus status);
}
