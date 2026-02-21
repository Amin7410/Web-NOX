package com.nox.platform.module.iam.api.response;

import java.util.List;

public record MfaEnableResponse(
        List<String> backupCodes) {
}
