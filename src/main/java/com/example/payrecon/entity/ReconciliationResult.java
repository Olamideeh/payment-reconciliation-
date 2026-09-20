package com.example.payrecon.entity;

import com.example.payrecon.enums.ReconciliationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reconciliation_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_batch_reference",
                        columnNames = {"batch_id", "reference"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ReconciliationBatch batch;

    @Column(nullable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_transaction_id")
    private TransactionRecord internalTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_transaction_id")
    private TransactionRecord providerTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status;

    @Column(nullable = false)
    private boolean requiresInvestigation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void prepareResult() {
        requiresInvestigation =
                status != null &&
                        status != ReconciliationStatus.MATCHED;

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}