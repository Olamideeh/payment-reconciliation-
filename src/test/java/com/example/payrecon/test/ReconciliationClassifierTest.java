

package com.example.payrecon.test;

import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.enums.ReconciliationStatus;
import com.example.payrecon.enums.TransactionStatus;
import com.example.payrecon.service.ReconciliationClassifier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationClassifierTest {

    private final ReconciliationClassifier classifier =
            new ReconciliationClassifier();

    @Test
    void shouldReturnMatchedWhenAmountAndStatusAreTheSame() {

        TransactionRecord internalTransaction =
                TransactionRecord.builder()
                        .reference("TXN001")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        TransactionRecord providerTransaction =
                TransactionRecord.builder()
                        .reference("TXN001")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        ReconciliationStatus result = classifier.classify(
                internalTransaction,
                providerTransaction,
                false
        );

        assertEquals(ReconciliationStatus.MATCHED, result);
    }

    @Test
    void shouldReturnDuplicateWhenReferenceAppearsMoreThanOnce() {

        TransactionRecord internalTransaction =
                TransactionRecord.builder()
                        .reference("TXN006")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        TransactionRecord providerTransaction =
                TransactionRecord.builder()
                        .reference("TXN006")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        ReconciliationStatus result = classifier.classify(
                internalTransaction,
                providerTransaction,
                true
        );

        assertEquals(ReconciliationStatus.DUPLICATE, result);
    }

    @Test
    void shouldReturnMissingInternallyWhenInternalTransactionIsNull() {

        TransactionRecord providerTransaction =
                TransactionRecord.builder()
                        .reference("TXN007")
                        .amount(new BigDecimal("2000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        ReconciliationStatus result = classifier.classify(
                null,
                providerTransaction,
                false
        );

        assertEquals(
                ReconciliationStatus.MISSING_INTERNALLY,
                result
        );
    }
    @Test
    void shouldReturnMissingFromProviderWhenProviderTransactionIsNull() {

        TransactionRecord internalTransaction =
                TransactionRecord.builder()
                        .reference("TXN004")
                        .amount(new BigDecimal("3000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        ReconciliationStatus result = classifier.classify(
                internalTransaction,
                null,
                false
        );

        assertEquals(
                ReconciliationStatus.MISSING_FROM_PROVIDER,
                result
        );
    }

    @Test
    void shouldReturnAmountMismatchWhenAmountsAreDifferent() {

        TransactionRecord internalTransaction =
                TransactionRecord.builder()
                        .reference("TXN002")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        TransactionRecord providerTransaction =
                TransactionRecord.builder()
                        .reference("TXN002")
                        .amount(new BigDecimal("4500.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        ReconciliationStatus result = classifier.classify(
                internalTransaction,
                providerTransaction,
                false
        );

        assertEquals(
                ReconciliationStatus.AMOUNT_MISMATCH,
                result
        );
    }

    @Test
    void shouldReturnStatusMismatchWhenStatusesAreDifferent() {

        TransactionRecord internalTransaction =
                TransactionRecord.builder()
                        .reference("TXN003")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.SUCCESS)
                        .build();

        TransactionRecord providerTransaction =
                TransactionRecord.builder()
                        .reference("TXN003")
                        .amount(new BigDecimal("5000.00"))
                        .status(TransactionStatus.FAILED)
                        .build();

        ReconciliationStatus result = classifier.classify(
                internalTransaction,
                providerTransaction,
                false
        );

        assertEquals(
                ReconciliationStatus.STATUS_MISMATCH,
                result
        );
    }

}

