package com.nox.platform.module.tenant.service;

import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.shared.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class Phase17VerificationTest {

    @Autowired
    private RoleService roleService;

    @MockBean
    private OrgMemberRepository orgMemberRepository;

    @MockBean
    private AuditService auditService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @Test
    @WithMockUser(username = "admin@nox.com")
    void verifyAuditLoggingOnRoleCreation() {
        UUID orgId = UUID.randomUUID();
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(orgId);

        User admin = User.builder().id(UUID.randomUUID()).email("admin@nox.com").build();

        when(userRepository.findByEmail("admin@nox.com")).thenReturn(Optional.of(admin));

        Role mockRole = Role.builder().name("TEST_ROLE").build();
        when(roleRepository.save(any())).thenReturn(mockRole);

        roleService.createRole(org, "TEST_ROLE", Collections.emptyList());

        // Verify that AuditService.record was called by the AuditAspect
        verify(auditService, atLeastOnce()).record(
                eq(orgId),
                eq(admin.getId()),
                eq("createRole"),
                anyString(),
                any(),
                anyMap(),
                any(),
                any());
    }
}
