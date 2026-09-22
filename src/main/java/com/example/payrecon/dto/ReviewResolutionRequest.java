package com.example.payrecon.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewResolutionRequest(

        @NotBlank(message = "Review comment is required")
        String reviewComment

) {
}