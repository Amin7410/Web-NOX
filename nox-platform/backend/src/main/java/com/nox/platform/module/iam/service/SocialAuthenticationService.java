package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.domain.SocialIdentity;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.domain.UserSecurity;
import com.nox.platform.module.iam.domain.UserStatus;
import com.nox.platform.module.iam.infrastructure.SocialIdentityRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Service handling OAuth2 operations intercepting Provider payload
 * configurations separating
 * it from Base Authentication patterns.
 */
@Service
@RequiredArgsConstructor
public class SocialAuthenticationService {

    private final UserRepository userRepository;
    private final SocialIdentityRepository socialIdentityRepository;
    private final SocialAuthVerificationService socialAuthVerificationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserSessionService userSessionService;

    @Transactional
    public AuthenticationService.AuthResult socialLogin(String provider, String token, String ipAddress,
            String userAgent) {
        Map<String, Object> verifiedData = socialAuthVerificationService.verifyToken(provider, token);

        String providerId = (String) verifiedData.get("providerId");
        String email = ((String) verifiedData.get("email")).trim().toLowerCase();
        String fullName = (String) verifiedData.get("fullName");

        @SuppressWarnings("unchecked")
        Map<String, Object> profileData = (Map<String, Object>) verifiedData.get("rawProfile");

        Optional<SocialIdentity> existingIdentity = socialIdentityRepository.findByProviderAndProviderId(provider,
                providerId);
        User user;

        if (existingIdentity.isPresent()) {
            user = existingIdentity.get().getUser();
        } else {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                // Potential Account Takeover: email found but no linked social identity
                // If user has a password set, we MUST ask for it before linking
                if (user.getSecurity().isPasswordSet()) {
                    throw new DomainException("LINK_REQUIRED",
                            "Account exists. Please link your social account with your password.", 403);
                }
            }

            if (user == null) {
                user = User.builder()
                        .email(email)
                        .fullName(fullName)
                        .status(UserStatus.ACTIVE)
                        .isEmailVerified(true)
                        .build();

                UserSecurity security = UserSecurity.builder()
                        .user(user)
                        .isPasswordSet(false)
                        .build();
                user.setSecurity(security);
                user = userRepository.save(user);
            }

            SocialIdentity identity = SocialIdentity.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .profileData(profileData)
                    .build();
            socialIdentityRepository.save(identity);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Your account is not active or has been suspended.", 403);
        }

        if (user.getSecurity() != null && user.getSecurity().isMfaEnabled()) {
            String mfaToken = jwtService.generateToken(java.util.Map.of("mfa_pending", true), user.getEmail());
            return new AuthenticationService.AuthResult(null, null, null, true, mfaToken);
        }

        return userSessionService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }

    @Transactional
    public AuthenticationService.AuthResult linkSocialAccount(String provider, String token, String password,
            String ipAddress,
            String userAgent) {
        Map<String, Object> verifiedData = socialAuthVerificationService.verifyToken(provider, token);
        String email = ((String) verifiedData.get("email")).trim().toLowerCase();
        String providerId = (String) verifiedData.get("providerId");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found", 404));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DomainException("ACCOUNT_NOT_ACTIVE", "Account is not active", 403);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            throw new DomainException("INVALID_CREDENTIALS", "Invalid password for linking", 401);
        }

        // Check again if already linked (race condition)
        if (socialIdentityRepository.findByProviderAndProviderId(provider, providerId).isPresent()) {
            return userSessionService.generateSuccessAuthResult(user, ipAddress, userAgent);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> profileData = (Map<String, Object>) verifiedData.get("rawProfile");

        SocialIdentity identity = SocialIdentity.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .profileData(profileData)
                .build();
        socialIdentityRepository.save(identity);

        return userSessionService.generateSuccessAuthResult(user, ipAddress, userAgent);
    }
}
