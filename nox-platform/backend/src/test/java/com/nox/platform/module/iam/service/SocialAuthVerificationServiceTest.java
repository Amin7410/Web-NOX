package com.nox.platform.module.iam.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAuthVerificationServiceTest {

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    private SocialAuthVerificationService socialAuthService;

    @BeforeEach
    void setUp() {
        socialAuthService = new SocialAuthVerificationService("dummy-client-id");
        ReflectionTestUtils.setField(socialAuthService, "googleIdTokenVerifier", googleIdTokenVerifier);
    }

    @Test
    void verifyToken_InvalidProvider_ThrowsException() {
        assertThatThrownBy(() -> socialAuthService.verifyToken("unsupported", "token123"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Failed to securely verify the social token");
    }

    @Test
    void verifyToken_EmptyInputs_ThrowsException() {
        assertThatThrownBy(() -> socialAuthService.verifyToken("google", ""))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Provider and token must not be empty");
    }

    @Test
    void verifyToken_Google_Success() throws Exception {
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();
        mockPayload.setSubject("1234567890");
        mockPayload.setEmail("test@gmail.com");
        mockPayload.set("name", "Test User");

        when(mockToken.getPayload()).thenReturn(mockPayload);
        when(googleIdTokenVerifier.verify("valid-token")).thenReturn(mockToken);

        Map<String, Object> result = socialAuthService.verifyToken("google", "valid-token");

        assertThat(result).isNotNull();
        assertThat(result.get("providerId")).isEqualTo("1234567890");
        assertThat(result.get("email")).isEqualTo("test@gmail.com");
        assertThat(result.get("fullName")).isEqualTo("Test User");
    }

    @Test
    void verifyToken_Google_InvalidToken_ThrowsException() throws Exception {
        when(googleIdTokenVerifier.verify("invalid-token")).thenReturn(null);

        assertThatThrownBy(() -> socialAuthService.verifyToken("google", "invalid-token"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid ID token.");
    }

    @Test
    void verifyToken_Google_MissingEmail_ThrowsException() throws Exception {
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();
        mockPayload.setSubject("1234567890");

        when(mockToken.getPayload()).thenReturn(mockPayload);
        when(googleIdTokenVerifier.verify("valid-token")).thenReturn(mockToken);

        assertThatThrownBy(() -> socialAuthService.verifyToken("google", "valid-token"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Missing email.");
    }

    @Test
    void verifyToken_Google_Exception_ThrowsException() throws Exception {
        when(googleIdTokenVerifier.verify("valid-token")).thenThrow(new RuntimeException("Simulated error"));

        assertThatThrownBy(() -> socialAuthService.verifyToken("google", "valid-token"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Failed to securely verify the Google token across network boundaries.");
    }
}
