package com.nox.platform.module.iam.service.abstraction;

public interface MfaProvider {
    String generateSecret();
    String getQrCodeUri(String secret, String accountName, String issuer);
    boolean verifyCode(String secret, int code);
}
