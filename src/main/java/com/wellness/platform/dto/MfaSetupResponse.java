package com.wellness.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MfaSetupResponse {
    private String secret;
    private String qrCodeUri;
}
