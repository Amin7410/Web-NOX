package com.nox.platform.module.iam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MfaServiceTest {

    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        mfaService = new MfaService();
    }

    @Test
    void generateSecretKey_returnsValidString() {
        String secret = mfaService.generateSecretKey();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
        // Base32 typical length for TOTP secrets
        assertTrue(secret.length() >= 16);
    }

    @Test
    void getQrCodeUri_formatsCorrectly() {
        String secret = "JBSWY3DPEHPK3PXP";
        String email = "test@nox.com";
        String uri = mfaService.getQrCodeUri(secret, email);

        assertNotNull(uri);
        assertTrue(uri.startsWith("otpauth://totp/NOX-Platform:test@nox.com"));
        assertTrue(uri.contains("secret=" + secret));
        assertTrue(uri.contains("issuer=NOX-Platform"));
    }

    @Test
    void verifyCode_withInvalidCode_returnsFalse() {
        String secret = mfaService.generateSecretKey();
        // Almost mathematically impossible to guess 6 digits randomly correctly.
        // We ensure 999999 is highly likely false
        boolean isValid = mfaService.verifyCode(secret, 999999);

        assertFalse(isValid, "Randomly guessing the code should fail");
    }
}
