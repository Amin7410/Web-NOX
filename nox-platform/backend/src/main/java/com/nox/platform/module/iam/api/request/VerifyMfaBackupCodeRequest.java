package com.nox.platform.module.iam.api.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyMfaBackupCodeRequest(
        @NotBlank(message = "MFA Token is required") String mfaToken,
        @NotBlank(message = "Backup Code is required") String backupCode) {
}
