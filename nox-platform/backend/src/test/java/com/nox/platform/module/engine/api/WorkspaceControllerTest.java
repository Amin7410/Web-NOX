package com.nox.platform.module.engine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.engine.api.request.CreateWorkspaceRequest;
import com.nox.platform.module.engine.api.response.WorkspaceResponse;
import com.nox.platform.module.engine.domain.WorkspaceType;
import com.nox.platform.module.engine.service.WorkspaceService;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.shared.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private WorkspaceService workspaceService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private OrgMemberRepository orgMemberRepository;

        @MockBean
        private JwtService jwtService;

        private UUID mockProjectId;
        private UUID mockWorkspaceId;
        private UUID mockUserId;
        private MockedStatic<SecurityUtil> mockedSecurityUtil;

        @BeforeEach
        void setUp() {
                mockProjectId = UUID.randomUUID();
                mockWorkspaceId = UUID.randomUUID();
                mockUserId = UUID.randomUUID();
                mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
                mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(mockUserId);
        }

        @AfterEach
        void tearDown() {
                mockedSecurityUtil.close();
        }

        @Test
        void createWorkspace_success() throws Exception {
                CreateWorkspaceRequest request = new CreateWorkspaceRequest("Backend", WorkspaceType.BACKEND);
                WorkspaceResponse response = new WorkspaceResponse(
                                mockWorkspaceId, mockProjectId, "Backend", WorkspaceType.BACKEND,
                                com.nox.platform.module.engine.domain.WorkspaceStatus.DRAFT,
                                mockUserId, OffsetDateTime.now());

                Mockito.when(workspaceService.createWorkspace(eq(mockProjectId), any(CreateWorkspaceRequest.class),
                                eq(mockUserId)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/v1/projects/{projectId}/workspaces", mockProjectId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(mockWorkspaceId.toString()))
                                .andExpect(jsonPath("$.name").value("Backend"))
                                .andExpect(jsonPath("$.type").value("BACKEND"));
        }

        @Test
        void getWorkspacesByProject_success() throws Exception {
                WorkspaceResponse response = new WorkspaceResponse(
                                mockWorkspaceId, mockProjectId, "Backend", WorkspaceType.BACKEND,
                                com.nox.platform.module.engine.domain.WorkspaceStatus.DRAFT,
                                mockUserId, OffsetDateTime.now());

                Mockito.when(workspaceService.getWorkspacesByProject(mockProjectId)).thenReturn(List.of(response));

                mockMvc.perform(get("/api/v1/projects/{projectId}/workspaces", mockProjectId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Backend"));
        }

        @Test
        void deleteWorkspace_success() throws Exception {
                mockMvc.perform(delete("/api/v1/projects/{projectId}/workspaces/{workspaceId}", mockProjectId,
                                mockWorkspaceId))
                                .andExpect(status().isNoContent());

                Mockito.verify(workspaceService).deleteWorkspace(mockWorkspaceId);
        }
}
