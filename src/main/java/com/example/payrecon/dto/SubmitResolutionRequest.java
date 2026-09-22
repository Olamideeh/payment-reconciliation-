package com.example.payrecon.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitResolutionRequest(

        @NotBlank(message = "Proposed resolution is required")
        String proposedResolution

) {
}