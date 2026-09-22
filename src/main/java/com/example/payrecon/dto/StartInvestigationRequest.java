package com.example.payrecon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartInvestigationRequest {

        @NotBlank(message = "Investigation note is required")
        private String investigationNote;
}