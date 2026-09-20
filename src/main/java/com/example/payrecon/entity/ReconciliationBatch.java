package com.example.payrecon.entity;

import com.example.payrecon.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_batches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String internalFileName;

    @Column(nullable = false)
    private String providerFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @Column(nullable = false)
    private String uploadedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ReconciliationBatch batch;

    private LocalDateTime completedAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();

        if (status == null) {
            status = BatchStatus.UPLOADED;
        }
    }
}