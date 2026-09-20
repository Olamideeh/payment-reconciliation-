package com.example.payrecon.service;

import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.entity.ReconciliationBatch;
import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.enums.BatchStatus;
import com.example.payrecon.enums.CaseStatus;
import com.example.payrecon.enums.ReconciliationStatus;
import com.example.payrecon.enums.TransactionSource;
import com.example.payrecon.repository.InvestigationCaseRepository;
import com.example.payrecon.repository.ReconciliationBatchRepository;
import com.example.payrecon.repository.ReconciliationResultRepository;
import com.example.payrecon.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReconciliationEngineService {

    private final ReconciliationBatchRepository batchRepository;
    private final TransactionRecordRepository transactionRepository;
    private final ReconciliationResultRepository resultRepository;
    private final InvestigationCaseRepository caseRepository;
    private final ReconciliationClassifier classifier;

    @Transactional
    public List<ReconciliationResult> reconcile(Long batchId) {

        // 1. Find the batch
        ReconciliationBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new RuntimeException("Reconciliation batch not found")
                );

        if (batch.getStatus() == BatchStatus.COMPLETED) {
            throw new IllegalStateException(
                    "This batch has already been reconciled"
            );
        }

        batch.setStatus(BatchStatus.PROCESSING);

        // 2. Get every transaction belonging to the batch
        List<TransactionRecord> transactions =
                transactionRepository.findAllByBatch_Id(batchId);

        // 3. Group internal transactions by reference
        Map<String, List<TransactionRecord>> internalTransactions =
                transactions.stream()
                        .filter(transaction ->
                                transaction.getSource()
                                        == TransactionSource.INTERNAL
                        )
                        .collect(Collectors.groupingBy(
                                TransactionRecord::getReference
                        ));

        // 4. Group provider transactions by reference
        Map<String, List<TransactionRecord>> providerTransactions =
                transactions.stream()
                        .filter(transaction ->
                                transaction.getSource()
                                        == TransactionSource.PROVIDER
                        )
                        .collect(Collectors.groupingBy(
                                TransactionRecord::getReference
                        ));

        // 5. Collect every reference from both files
        Set<String> references = new HashSet<>();

        references.addAll(internalTransactions.keySet());
        references.addAll(providerTransactions.keySet());

        List<ReconciliationResult> results = new ArrayList<>();

        // 6. Reconcile each reference
        for (String reference : references) {

            List<TransactionRecord> internalMatches =
                    internalTransactions.getOrDefault(
                            reference,
                            Collections.emptyList()
                    );

            List<TransactionRecord> providerMatches =
                    providerTransactions.getOrDefault(
                            reference,
                            Collections.emptyList()
                    );

            boolean duplicate =
                    internalMatches.size() > 1 ||
                            providerMatches.size() > 1;

            TransactionRecord internalTransaction =
                    internalMatches.isEmpty()
                            ? null
                            : internalMatches.get(0);

            TransactionRecord providerTransaction =
                    providerMatches.isEmpty()
                            ? null
                            : providerMatches.get(0);

            ReconciliationStatus status = classifier.classify(
                    internalTransaction,
                    providerTransaction,
                    duplicate
            );

            // 7. Save the result
            ReconciliationResult result =
                    ReconciliationResult.builder()
                            .batch(batch)
                            .reference(reference)
                            .internalTransaction(internalTransaction)
                            .providerTransaction(providerTransaction)
                            .status(status)
                            .build();

            ReconciliationResult savedResult =
                    resultRepository.save(result);

            results.add(savedResult);

            // 8. Create a case for every result except MATCHED
            if (status != ReconciliationStatus.MATCHED) {

                InvestigationCase investigationCase =
                        InvestigationCase.builder()
                                .reconciliationResult(savedResult)
                                .status(CaseStatus.OPEN)
                                .build();

                caseRepository.save(investigationCase);
            }
        }

        // 9. Mark the reconciliation as completed
        batch.setStatus(BatchStatus.COMPLETED);
        batch.setCompletedAt(LocalDateTime.now());
        batchRepository.save(batch);

        return results;
    }
}