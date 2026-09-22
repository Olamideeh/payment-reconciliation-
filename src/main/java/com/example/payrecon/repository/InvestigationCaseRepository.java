package com.example.payrecon.repository;

import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface InvestigationCaseRepository
        extends JpaRepository<InvestigationCase, Long> {

    Optional<InvestigationCase> findByResult_Id(Long resultId);
    Page<InvestigationCase> findAllByStatus(
            CaseStatus status,
            Pageable pageable
    );
    List<InvestigationCase> findAllByResult_Batch_Id(Long batchId);
}