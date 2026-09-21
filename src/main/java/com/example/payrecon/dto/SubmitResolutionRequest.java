package com.example.payrecon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitResolutionRequest(

        @NotBlank(message = "Proposed resolution is required")
        @Size(
                max = 2000,
                message = "Proposed resolution cannot exceed 2000 characters"
        )
        String proposedResolution,

        @NotBlank(message = "Officer name is required")
        String submittedBy
) {
}