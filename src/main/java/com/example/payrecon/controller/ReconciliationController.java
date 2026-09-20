package com.example.payrecon.controller;

import com.example.payrecon.dto.BatchUploadResponse;
import com.example.payrecon.entity.ReconciliationBatch;
import com.example.payrecon.service.ReconciliationUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reconciliations")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationUploadService uploadService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BatchUploadResponse> uploadFiles(
            @RequestParam("internalFile")
            MultipartFile internalFile,

            @RequestParam("providerFile")
            MultipartFile providerFile,

            @RequestParam("uploadedBy")
            String uploadedBy
    ){

        ReconciliationBatch batch = uploadService.upload(
                internalFile,
                providerFile,
                uploadedBy
        );

        BatchUploadResponse response =
                new BatchUploadResponse(
                        batch.getId(),
                        batch.getInternalFileName(),
                        batch.getProviderFileName(),
                        batch.getUploadedBy(),
                        batch.getStatus(),
                        batch.getCreatedAt(),
                        "CSV files uploaded successfully"
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }
}