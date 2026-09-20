package com.example.payrecon.repository;

import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.enums.TransactionSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRecordRepository
        extends JpaRepository<TransactionRecord, Long> {

    List<TransactionRecord> findAllByBatch_Id(Long batchId);

    List<TransactionRecord> findAllByBatch_IdAndSource(
            Long batchId,
            TransactionSource source
    );

    List<TransactionRecord> findAllByBatch_IdAndReference(
            Long batchId,
            String reference
    );
}