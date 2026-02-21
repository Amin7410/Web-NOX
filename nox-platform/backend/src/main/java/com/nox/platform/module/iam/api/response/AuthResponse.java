package com.nox.platform.module.iam.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String id,
        String email,
        String token,
        String refreshToken,
        Boolean mfaRequired,
        String mfaToken) {
}
