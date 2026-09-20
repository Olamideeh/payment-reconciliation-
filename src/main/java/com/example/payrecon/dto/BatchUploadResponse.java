package com.example.payrecon.dto;

import com.example.payrecon.enums.BatchStatus;

import java.time.LocalDateTime;

public record BatchUploadResponse(
        Long batchId,
        String internalFileName,
        String providerFileName,
        String uploadedBy,
        BatchStatus status,
        LocalDateTime createdAt,
        String message
)
{
}