package com.nox.platform.module.iam.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * Generates a new TOTP secret key for the user.
     * 
     * @return The secret key as a String.
     */
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Generates an otpauth:// URI which can be used to generate a QR Code.
     * 
     * @param secret The user's MFA secret.
     * @param email  The user's email address.
     * @return The otpauth URI string.
     */
    public String getQrCodeUri(String secret, String email) {
        // Format: otpauth://totp/Issuer:AccountName?secret=Secret&issuer=Issuer
        String issuer = "NOX-Platform";
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, email, secret, issuer);
    }

    /**
     * Validates a 6-digit TOTP code against a secret key.
     * 
     * @param secret The secret key to validate against.
     * @param code   The 6-digit code provided by the user.
     * @return true if the code is valid, false otherwise.
     */
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
