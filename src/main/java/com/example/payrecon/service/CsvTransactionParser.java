package com.example.payrecon.service;

import com.example.payrecon.entity.ReconciliationBatch;
import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.enums.TransactionSource;
import com.example.payrecon.enums.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvTransactionParser {

    private static final String EXPECTED_HEADER =
            "reference,amount,status,transactionDate";

    public List<TransactionRecord> parse(
            MultipartFile file,
            TransactionSource source,
            ReconciliationBatch batch
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file cannot be empty");
        }

        List<TransactionRecord> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                )
        )) {

            String header = reader.readLine();

            if (header == null ||
                    !header.trim().equalsIgnoreCase(EXPECTED_HEADER)) {

                throw new IllegalArgumentException(
                        "Invalid CSV header. Expected: " + EXPECTED_HEADER
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);

                if (columns.length != 4) {
                    throw invalidRow(
                            lineNumber,
                            "Exactly four columns are required"
                    );
                }

                String reference = columns[0].trim();
                String amountValue = columns[1].trim();
                String statusValue = columns[2].trim();
                String dateValue = columns[3].trim();

                if (reference.isBlank()) {
                    throw invalidRow(
                            lineNumber,
                            "Reference cannot be empty"
                    );
                }

                BigDecimal amount;

                try {
                    amount = new BigDecimal(amountValue);
                } catch (NumberFormatException exception) {
                    throw invalidRow(
                            lineNumber,
                            "Amount must be a valid number"
                    );
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw invalidRow(
                            lineNumber,
                            "Amount must be greater than zero"
                    );
                }

                TransactionStatus status;

                try {
                    status = TransactionStatus.valueOf(
                            statusValue.toUpperCase()
                    );
                } catch (IllegalArgumentException exception) {
                    throw invalidRow(
                            lineNumber,
                            "Unknown transaction status: " + statusValue
                    );
                }

                LocalDate transactionDate;

                try {
                    transactionDate = LocalDate.parse(dateValue);
                } catch (DateTimeParseException exception) {
                    throw invalidRow(
                            lineNumber,
                            "Date must use yyyy-MM-dd format"
                    );
                }

                TransactionRecord transaction =
                        TransactionRecord.builder()
                                .reference(reference)
                                .amount(amount)
                                .status(status)
                                .transactionDate(transactionDate)
                                .source(source)
                                .batch(batch)
                                .build();

                transactions.add(transaction);
            }

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not read the CSV file",
                    exception
            );
        }

        return transactions;
    }

    private IllegalArgumentException invalidRow(
            int lineNumber,
            String message
    ) {
        return new IllegalArgumentException(
                "Invalid CSV row " + lineNumber + ": " + message
        );
    }
}