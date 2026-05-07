package com.wellness.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String password;
}
