package com.example.payrecon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewResolutionRequest(

        @NotBlank(message = "Admin name is required")
        String reviewedBy,

        @NotBlank(message = "Review comment is required")
        @Size(
                max = 2000,
                message = "Review comment cannot exceed 2000 characters"
        )
        String reviewComment
) {
}