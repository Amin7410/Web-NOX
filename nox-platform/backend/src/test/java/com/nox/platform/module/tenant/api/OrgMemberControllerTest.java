package com.nox.platform.module.tenant.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.iam.service.InvitationService;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.RoleRepository;
import com.nox.platform.module.tenant.service.OrgMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrgMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrgMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrgMemberService orgMemberService;

    @MockBean
    private InvitationService invitationService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private OrgMemberRepository orgMemberRepository;

    @Test
    @WithMockUser(username = "inviter@nox.com")
    void inviteMember_Success() throws Exception {
        UUID orgId = UUID.randomUUID();
        String requestJson = """
                {
                  "email": "newuser@nox.com",
                  "roleName": "EDITOR"
                }
                """;

        Role mockRole = Role.builder().name("EDITOR").build();
        mockRole.setId(UUID.randomUUID());

        User mockInviter = User.builder().email("inviter@nox.com").build();
        mockInviter.setId(UUID.randomUUID());

        when(roleRepository.findByOrganizationIdAndName(orgId, "EDITOR")).thenReturn(Optional.of(mockRole));
        when(userRepository.findByEmail("inviter@nox.com")).thenReturn(Optional.of(mockInviter));
        doNothing().when(invitationService).inviteUser(eq("newuser@nox.com"), eq(orgId), eq(mockRole.getId()),
                eq(mockInviter.getId()));

        mockMvc.perform(post("/api/v1/orgs/{orgId}/members", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value("Invitation sent successfully to newuser@nox.com"));
    }

    @Test
    void getMembers_Success() throws Exception {
        UUID orgId = UUID.randomUUID();

        User user = User.builder().email("test@nox.com").fullName("Test User").build();
        user.setId(UUID.randomUUID());

        Role role = Role.builder().name("ADMIN").permissions(List.of("*")).build();
        role.setId(UUID.randomUUID());

        OrgMember member = OrgMember.builder()
                .organization(Organization.builder().build())
                .user(user)
                .role(role)
                .build();
        member.setId(UUID.randomUUID());

        Page<OrgMember> memberPage = new PageImpl<>(List.of(member), PageRequest.of(0, 10), 1);

        when(orgMemberService.getMembersByOrganization(eq(orgId), any(Pageable.class))).thenReturn(memberPage);

        mockMvc.perform(get("/api/v1/orgs/{orgId}/members", orgId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("test@nox.com"))
                .andExpect(jsonPath("$.data.content[0].role.name").value("ADMIN"));
    }

    @Test
    void removeMember_Success() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(orgMemberService).removeMember(orgId, userId);

        mockMvc.perform(delete("/api/v1/orgs/{orgId}/members/{userId}", orgId, userId))
                .andExpect(status().isNoContent());

        verify(orgMemberService).removeMember(orgId, userId);
    }
}
