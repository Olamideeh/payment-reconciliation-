package com.example.payrecon.service;

import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.enums.ReconciliationStatus;
import com.example.payrecon.enums.TransactionSource;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationClassifier {

    public ReconciliationStatus classify(
            TransactionRecord internalTransaction,
            TransactionRecord providerTransaction,
            boolean duplicate
    ) {

        if (duplicate) {
            return ReconciliationStatus.DUPLICATE;
        }

        if (internalTransaction == null) {
            return ReconciliationStatus.MISSING_INTERNALLY;
        }

        if (providerTransaction == null) {
            return ReconciliationStatus.MISSING_FROM_PROVIDER;
        }

        if (internalTransaction.getAmount()
                .compareTo(providerTransaction.getAmount()) != 0) {

            return ReconciliationStatus.AMOUNT_MISMATCH;
        }

        if (internalTransaction.getStatus()
                != providerTransaction.getStatus()) {

            return ReconciliationStatus.STATUS_MISMATCH;
        }

        return ReconciliationStatus.MATCHED;
    }
    }
