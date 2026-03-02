package com.nox.platform.shared.util;

import com.nox.platform.shared.security.NoxUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class SecurityUtilTest {

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = Mockito.mock(SecurityContext.class);
        authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_withValidNoxUserDetails_returnsId() {
        UUID expectedId = UUID.randomUUID();
        NoxUserDetails userDetails = Mockito.mock(NoxUserDetails.class);
        when(userDetails.getId()).thenReturn(expectedId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        UUID actualId = SecurityUtil.getCurrentUserId();

        assertEquals(expectedId, actualId);
    }

    @Test
    void getCurrentUserId_withNoAuthentication_returnsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        UUID actualId = SecurityUtil.getCurrentUserId();

        assertNull(actualId);
    }

    @Test
    void getCurrentUserId_withNonNoxUserDetailsPrincipal_returnsNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        UUID actualId = SecurityUtil.getCurrentUserId();

        assertNull(actualId);
    }

    @Test
    void getCurrentUserEmail_withValidUserDetails_returnsEmail() {
        String expectedEmail = "user@test.com";
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(expectedEmail);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String actualEmail = SecurityUtil.getCurrentUserEmail();

        assertEquals(expectedEmail, actualEmail);
    }

    @Test
    void getCurrentUserEmail_withInvalidState_returnsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        String nullEmailContext = SecurityUtil.getCurrentUserEmail();
        assertNull(nullEmailContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        String badPrincipal = SecurityUtil.getCurrentUserEmail();
        assertNull(badPrincipal);
    }
}
