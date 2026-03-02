package com.nox.platform.module.tenant.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrgMemberRepository orgMemberRepository;

    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
    }

    @Test
    @WithMockUser(username = "test@nox.com")
    void createOrganization_Success() throws Exception {
        String requestJson = """
                {
                  "name": "Acme Corp"
                }
                """;

        Organization org = Organization.builder().name("Acme Corp").slug("acme-corp").build();
        org.setId(orgId);

        when(organizationService.createOrganization(eq("Acme Corp"), eq("test@nox.com"))).thenReturn(org);

        mockMvc.perform(post("/api/v1/orgs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Acme Corp"))
                .andExpect(jsonPath("$.data.slug").value("acme-corp"));
    }

    @Test
    void getOrganizationById_Success() throws Exception {
        Organization org = Organization.builder().name("Acme Corp").slug("acme-corp").build();
        org.setId(orgId);

        when(organizationService.getOrganizationById(orgId)).thenReturn(org);

        mockMvc.perform(get("/api/v1/orgs/{id}", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Acme Corp"));
    }

    @Test
    void updateOrganization_Success() throws Exception {
        String requestJson = """
                {
                  "name": "New Acme",
                  "settings": { "color": "red" }
                }
                """;

        Organization org = Organization.builder().name("New Acme").build();
        org.setId(orgId);
        org.setSettings(Map.of("color", "red"));

        when(organizationService.updateOrganization(eq(orgId), eq("New Acme"), anyMap())).thenReturn(org);

        mockMvc.perform(put("/api/v1/orgs/{id}", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Acme"))
                .andExpect(jsonPath("$.data.settings.color").value("red"));
    }

    @Test
    void deleteOrganization_Success() throws Exception {
        doNothing().when(organizationService).deleteOrganization(orgId);

        mockMvc.perform(delete("/api/v1/orgs/{id}", orgId))
                .andExpect(status().isNoContent());

        verify(organizationService).deleteOrganization(orgId);
    }
}
