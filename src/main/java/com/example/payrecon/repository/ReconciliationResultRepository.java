package com.example.payrecon.repository;

import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.enums.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReconciliationResultRepository
        extends JpaRepository<ReconciliationResult, Long> {

    List<ReconciliationResult> findAllByBatch_Id(Long batchId);

    List<ReconciliationResult> findAllByBatch_IdAndStatus(
            Long batchId,
            ReconciliationStatus status
    );

    Optional<ReconciliationResult> findByBatch_IdAndReference(
            Long batchId,
            String reference
    );

    long countByBatch_IdAndStatus(
            Long batchId,
            ReconciliationStatus status
    );
}