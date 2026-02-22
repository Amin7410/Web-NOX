package com.nox.platform.module.tenant.infrastructure.security;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class TenantContextFilterTest {

    @Mock
    private OrgMemberRepository orgMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TenantContextFilter tenantContextFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User mockUser;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        orgId = UUID.randomUUID();

        mockUser = User.builder().email("test@example.com").build();
        mockUser.setId(UUID.randomUUID());
    }

    @Test
    void doFilterInternal_NoTenantHeader_ProceedsSilently() throws ServletException, IOException {
        tenantContextFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilterInternal_InvalidTenantHeader_Returns400() throws ServletException, IOException {
        request.addHeader("X-Org-Id", "not-a-uuid");
        tenantContextFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ValidHeader_ButNotAuthenticated_Returns401() throws ServletException, IOException {
        request.addHeader("X-Org-Id", orgId.toString());
        SecurityContextHolder.clearContext();
        tenantContextFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ValidHeaderAndAuth_ButNotMember_Returns403() throws ServletException, IOException {
        request.addHeader("X-Org-Id", orgId.toString());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test@example.com",
                "password123", java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, mockUser.getId())).thenReturn(Optional.empty());

        tenantContextFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ValidHeaderAndAuth_AndIsMember_Proceeds() throws ServletException, IOException {
        request.addHeader("X-Org-Id", orgId.toString());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test@example.com",
                "password123", java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        com.nox.platform.module.tenant.domain.Role mockRole = com.nox.platform.module.tenant.domain.Role.builder()
                .name("MEMBER").permissions(java.util.List.of("workspace:read")).build();
        com.nox.platform.module.tenant.domain.OrgMember mockMember = com.nox.platform.module.tenant.domain.OrgMember
                .builder()
                .role(mockRole).build();
        when(orgMemberRepository.findByOrganizationIdAndUserId(orgId, mockUser.getId()))
                .thenReturn(Optional.of(mockMember));

        tenantContextFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }
}
