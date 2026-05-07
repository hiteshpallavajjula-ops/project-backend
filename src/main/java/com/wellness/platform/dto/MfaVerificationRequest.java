package com.wellness.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerificationRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String code;
}
