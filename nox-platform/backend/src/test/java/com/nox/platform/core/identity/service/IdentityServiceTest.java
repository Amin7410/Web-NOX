package com.nox.platform.core.identity.service;

import com.nox.platform.api.dto.UserRegistrationRequest;
import com.nox.platform.core.identity.model.User;
import com.nox.platform.core.identity.model.UserSecurity;
import com.nox.platform.infra.persistence.identity.UserRepository;
import com.nox.platform.infra.persistence.identity.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private IdentityService identityService;

    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFullName("Test User");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        User savedUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .email(registrationRequest.getEmail())
                .fullName(registrationRequest.getFullName())
                .status("ACTIVE")
                .build();

        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = identityService.registerUser(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(registrationRequest.getEmail(), result.getEmail());
        assertEquals("ACTIVE", result.getStatus());

        verify(userRepository).save(any(User.class));
        verify(userSecurityRepository).save(any(UserSecurity.class));
        verify(passwordEncoder).encode(registrationRequest.getPassword());
    }
}
