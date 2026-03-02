package com.nox.platform.module.tenant.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.service.OrganizationService;
import com.nox.platform.module.tenant.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RoleService roleService;

        @MockBean
        private OrganizationService organizationService;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private OrgMemberRepository orgMemberRepository;

        @Test
        void createRole_Success() throws Exception {
                UUID orgId = UUID.randomUUID();
                Organization org = Organization.builder().name("Org").build();

                String requestJson = """
                                {
                                  "name": "EDITOR",
                                  "permissions": ["read", "write"]
                                }
                                """;

                Role mockRole = Role.builder().organization(org).name("EDITOR").permissions(List.of("read", "write"))
                                .level(10).build();
                mockRole.setId(UUID.randomUUID());

                when(organizationService.getOrganizationById(orgId)).thenReturn(org);
                when(roleService.createRole(eq(org), eq("EDITOR"), eq(List.of("read", "write")), any()))
                                .thenReturn(mockRole);

                mockMvc.perform(post("/api/v1/orgs/{orgId}/roles", orgId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.name").value("EDITOR"))
                                .andExpect(jsonPath("$.data.permissions[0]").value("read"));
        }

        @Test
        void getRoles_Success() throws Exception {
                UUID orgId = UUID.randomUUID();
                Organization org = Organization.builder().name("Org").build();
                when(organizationService.getOrganizationById(orgId)).thenReturn(org);

                Role mockRole = Role.builder().organization(org).name("ADMIN").permissions(List.of("*")).build();
                mockRole.setId(UUID.randomUUID());

                when(roleService.getRolesByOrganization(orgId)).thenReturn(List.of(mockRole));

                mockMvc.perform(get("/api/v1/orgs/{orgId}/roles", orgId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].name").value("ADMIN"));
        }

        @Test
        void deleteRole_Success() throws Exception {
                UUID orgId = UUID.randomUUID();
                String roleName = "EDITOR";

                doNothing().when(roleService).deleteRole(orgId, roleName);

                mockMvc.perform(delete("/api/v1/orgs/{orgId}/roles/{roleName}", orgId, roleName))
                                .andExpect(status().isNoContent());

                verify(roleService).deleteRole(orgId, roleName);
        }
}
