package com.example.payrecon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartInvestigationRequest(

        @NotBlank(message = "Officer name is required")
        String officer,

        @NotBlank(message = "Investigation note is required")
        @Size(
                max = 2000,
                message = "Investigation note cannot exceed 2000 characters"
        )
        String investigationNote
) {
}