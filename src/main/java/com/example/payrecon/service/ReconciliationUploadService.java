package com.example.payrecon.service;

import com.example.payrecon.entity.ReconciliationBatch;
import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.enums.BatchStatus;
import com.example.payrecon.enums.TransactionSource;
import com.example.payrecon.repository.ReconciliationBatchRepository;
import com.example.payrecon.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationUploadService {

    private final ReconciliationBatchRepository batchRepository;
    private final TransactionRecordRepository transactionRepository;
    private final CsvTransactionParser csvParser;

    @Transactional
    public ReconciliationBatch upload(
            MultipartFile internalFile,
            MultipartFile providerFile,
            String uploadedBy
    ) {

        validateCsvFile(internalFile, "Internal");
        validateCsvFile(providerFile, "Provider");

        ReconciliationBatch batch =
                ReconciliationBatch.builder()
                        .internalFileName(
                                getFileName(
                                        internalFile,
                                        "internal-transactions.csv"
                                )
                        )
                        .providerFileName(
                                getFileName(
                                        providerFile,
                                        "provider-transactions.csv"
                                )
                        )
                        .uploadedBy(uploadedBy)
                        .status(BatchStatus.UPLOADED)
                        .build();

        ReconciliationBatch savedBatch =
                batchRepository.save(batch);

        List<TransactionRecord> internalTransactions =
                csvParser.parse(
                        internalFile,
                        TransactionSource.INTERNAL,
                        savedBatch
                );

        List<TransactionRecord> providerTransactions =
                csvParser.parse(
                        providerFile,
                        TransactionSource.PROVIDER,
                        savedBatch
                );

        if (internalTransactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Internal CSV contains no transactions"
            );
        }

        if (providerTransactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Provider CSV contains no transactions"
            );
        }

        List<TransactionRecord> allTransactions =
                new ArrayList<>();

        allTransactions.addAll(internalTransactions);
        allTransactions.addAll(providerTransactions);

        transactionRepository.saveAll(allTransactions);

        return savedBatch;
    }

    private void validateCsvFile(
            MultipartFile file,
            String fileType
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    fileType + " CSV file cannot be empty"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase().endsWith(".csv")) {

            throw new IllegalArgumentException(
                    fileType + " file must be a CSV file"
            );
        }
    }

    private String getFileName(
            MultipartFile file,
            String defaultName
    ) {

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            return defaultName;
        }

        return fileName;
    }
}