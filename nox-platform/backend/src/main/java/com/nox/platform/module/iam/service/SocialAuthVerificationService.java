package com.nox.platform.module.iam.service;

import com.nox.platform.shared.exception.DomainException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class SocialAuthVerificationService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public SocialAuthVerificationService(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId) {
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(
                        googleClientId != null && !googleClientId.isBlank() ? Collections.singletonList(googleClientId)
                                : Collections.emptyList())
                .build();
    }

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

        if ("google".equalsIgnoreCase(provider)) {
            return verifyGoogleToken(token);
        }

        // Throw an exception if we can't verify the token
        throw new DomainException("INVALID_SOCIAL_TOKEN", "Failed to securely verify the social token", 401);
    }

    private Map<String, Object> verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                if (email == null) {
                    throw new DomainException("INVALID_SOCIAL_TOKEN",
                            "Google token verification failed. Missing email.", 401);
                }

                return Map.of(
                        "providerId", payload.getSubject(),
                        "email", email,
                        "fullName", payload.get("name") != null ? payload.get("name") : "",
                        "rawProfile", payload);
            } else {
                throw new DomainException("INVALID_SOCIAL_TOKEN", "Invalid ID token.", 401);
            }
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException("INVALID_SOCIAL_TOKEN",
                    "Failed to securely verify the Google token across network boundaries.", 401);
        }
    }
}
