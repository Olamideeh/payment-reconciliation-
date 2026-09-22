package com.example.payrecon.repository;

import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigationCaseRepository
        extends JpaRepository<InvestigationCase, Long> {

    Optional<InvestigationCase> findByResult_Id(Long resultId);

    List<InvestigationCase> findAllByStatus(CaseStatus status);

    List<InvestigationCase> findAllByResult_Batch_Id(Long batchId);
}