package com.example.payrecon.repository;

import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigationCaseRepository
        extends JpaRepository<InvestigationCase, Long> {

    Optional<InvestigationCase>
    findByReconciliationResult_Id(Long resultId);

    Page<InvestigationCase> findAllByStatus(
            CaseStatus status,
            Pageable pageable
    );

    List<InvestigationCase>
    findAllByReconciliationResult_Batch_Id(Long batchId);
}