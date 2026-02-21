package com.nox.platform.module.iam.api.response;

public record MfaSetupResponse(
        String secret,
        String qrCodeUri) {
}
