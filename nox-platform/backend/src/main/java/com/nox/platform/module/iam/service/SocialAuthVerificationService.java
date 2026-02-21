package com.nox.platform.module.iam.service;

import com.nox.platform.shared.exception.DomainException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SocialAuthVerificationService {

    /**
     * Verifies a third-party OAuth2 token and returns the normalized profile data.
     * In a real implementation, this would call Google/Facebook APIs.
     * 
     * @param provider e.g., "google", "facebook"
     * @param token    The id_token or access_token provided by the client
     * @return A map containing at minimum: 'email', 'providerId', and optionally
     *         'fullName'
     */
    public Map<String, Object> verifyToken(String provider, String token) {
        if (provider == null || token == null || token.isBlank()) {
            throw new DomainException("INVALID_SOCIAL_TOKEN", "Provider and token must not be empty", 400);
        }

        // --- MOCK IMPLEMENTATION FOR DEMONSTRATION ---
        // Warning: Replace this with real HTTP calls to Google's /oauth2/v3/tokeninfo
        // and Facebook's /me?access_token=... in production.

        if (!"google".equalsIgnoreCase(provider) && !"facebook".equalsIgnoreCase(provider)) {
            throw new DomainException("UNSUPPORTED_PROVIDER", "Unsupported social login provider", 400);
        }

        // For testing purposes, we assume the token is valid if it starts with "mock_"
        if (token.startsWith("mock_")) {
            return Map.of(
                    "providerId", "mock_id_" + token.substring(5),
                    "email", "user_" + token.substring(5) + "@mock.com",
                    "fullName", "Mock User " + token.substring(5),
                    "rawProfile", Map.of("picture", "https://example.com/avatar.jpg"));
        }

        // Throw an exception if we can't verify the token
        throw new DomainException("INVALID_SOCIAL_TOKEN", "Failed to securely verify the social token", 401);
    }
}
