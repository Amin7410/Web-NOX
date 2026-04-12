package com.nox.platform.module.iam.infrastructure.mfa;

import com.nox.platform.module.iam.service.abstraction.MfaProvider;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Component;

@Component
public class GoogleAuthenticatorProvider implements MfaProvider {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Override
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    @Override
    public String getQrCodeUri(String secret, String accountName, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, accountName, secret, issuer);
    }

    @Override
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
