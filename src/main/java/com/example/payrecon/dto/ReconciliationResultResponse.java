package com.example.payrecon.dto;

import com.example.payrecon.enums.ReconciliationStatus;
import com.example.payrecon.enums.TransactionStatus;

import java.math.BigDecimal;

public record ReconciliationResultResponse(
        Long resultId,
        String reference,
        BigDecimal internalAmount,
        TransactionStatus internalStatus,
        BigDecimal providerAmount,
        TransactionStatus providerStatus,
        ReconciliationStatus reconciliationStatus,
        boolean requiresInvestigation
) {
}