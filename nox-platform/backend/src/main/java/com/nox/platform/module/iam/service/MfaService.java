package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.service.abstraction.MfaProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final MfaProvider mfaProvider;

    public String generateSecretKey() {
        return mfaProvider.generateSecret();
    }

    public String getQrCodeUri(String secret, String email) {
        String issuer = "NOX-Platform";
        return mfaProvider.getQrCodeUri(secret, email, issuer);
    }

    public boolean verifyCode(String secret, int code) {
        return mfaProvider.verifyCode(secret, code);
    }
}
