package com.nox.platform.module.iam.infrastructure.social;

import com.nox.platform.module.iam.service.abstraction.SocialProvider;
import com.nox.platform.shared.exception.DomainException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class GoogleSocialProvider implements SocialProvider {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public GoogleSocialProvider(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId) {
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(
                        googleClientId != null && !googleClientId.isBlank() ? Collections.singletonList(googleClientId)
                                : Collections.emptyList())
                .build();
    }

    @Override
    public boolean supports(String providerName) {
        return "google".equalsIgnoreCase(providerName);
    }

    @Override
    public Map<String, Object> verifyToken(String idTokenString) {
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
