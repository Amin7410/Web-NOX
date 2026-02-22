package com.nox.platform.module.iam.service;

import com.nox.platform.shared.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SocialAuthVerificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

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

    private Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = restTemplate.getForObject(url, Map.class);

            if (claims == null || !claims.containsKey("email") || !claims.containsKey("sub")) {
                throw new DomainException("INVALID_SOCIAL_TOKEN",
                        "Google token verification failed. Missing email or sub.", 401);
            }

            // Verify audience if configured in application.yml
            String aud = (String) claims.get("aud");
            if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(aud)) {
                throw new DomainException("INVALID_SOCIAL_TOKEN",
                        "Audience mismatch. Potential token substitution attack.", 403);
            }

            return Map.of(
                    "providerId", claims.get("sub"),
                    "email", claims.get("email"),
                    "fullName", claims.getOrDefault("name", ""),
                    "rawProfile", claims);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException("INVALID_SOCIAL_TOKEN",
                    "Failed to securely verify the Google token across network boundaries.", 401);
        }
    }
}
