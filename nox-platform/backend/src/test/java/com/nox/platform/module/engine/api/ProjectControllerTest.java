package com.nox.platform.module.engine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.engine.api.request.CreateProjectRequest;
import com.nox.platform.module.engine.api.request.UpdateProjectRequest;
import com.nox.platform.module.engine.api.response.ProjectResponse;
import com.nox.platform.module.engine.domain.ProjectStatus;
import com.nox.platform.module.engine.domain.ProjectVisibility;
import com.nox.platform.module.engine.service.ProjectService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProjectService projectService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private OrgMemberRepository orgMemberRepository;

        @MockBean
        private JwtService jwtService;

        private UUID mockProjectId;
        private UUID mockUserId;
        private MockedStatic<SecurityUtil> mockedSecurityUtil;

        @BeforeEach
        void setUp() {
                mockProjectId = UUID.randomUUID();
                mockUserId = UUID.randomUUID();
                mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
                mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(mockUserId);
        }

        @AfterEach
        void tearDown() {
                mockedSecurityUtil.close();
        }

        @Test
        void createProject_success() throws Exception {
                CreateProjectRequest request = new CreateProjectRequest("App Name", "Desc", ProjectVisibility.PRIVATE);
                ProjectResponse response = new ProjectResponse(
                                mockProjectId, "App Name", "app-name", "Desc",
                                ProjectVisibility.PRIVATE, ProjectStatus.ACTIVE,
                                mockUserId, OffsetDateTime.now(), OffsetDateTime.now());

                Mockito.when(projectService.createProject(any(CreateProjectRequest.class), eq(mockUserId)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(mockProjectId.toString()))
                                .andExpect(jsonPath("$.name").value("App Name"))
                                .andExpect(jsonPath("$.slug").value("app-name"));
        }

        @Test
        void getProjects_success() throws Exception {
                ProjectResponse p1 = new ProjectResponse(
                                UUID.randomUUID(), "P1", "p1", null,
                                ProjectVisibility.PRIVATE, ProjectStatus.ACTIVE,
                                mockUserId, OffsetDateTime.now(), OffsetDateTime.now());
                Page<ProjectResponse> page = new PageImpl<>(List.of(p1));

                Mockito.when(projectService.getProjects(any(Pageable.class))).thenReturn(page);

                mockMvc.perform(get("/api/v1/projects?page=0&size=10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].name").value("P1"));
        }

        @Test
        void getProjectById_success() throws Exception {
                ProjectResponse response = new ProjectResponse(
                                mockProjectId, "P1", "p1", null,
                                ProjectVisibility.PRIVATE, ProjectStatus.ACTIVE,
                                mockUserId, OffsetDateTime.now(), OffsetDateTime.now());

                Mockito.when(projectService.getProjectById(mockProjectId)).thenReturn(response);

                mockMvc.perform(get("/api/v1/projects/{id}", mockProjectId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(mockProjectId.toString()));
        }

        @Test
        void getProjectBySlug_success() throws Exception {
                ProjectResponse response = new ProjectResponse(
                                mockProjectId, "P1", "p-1", null,
                                ProjectVisibility.PRIVATE, ProjectStatus.ACTIVE,
                                mockUserId, OffsetDateTime.now(), OffsetDateTime.now());

                Mockito.when(projectService.getProjectBySlug("p-1")).thenReturn(response);

                mockMvc.perform(get("/api/v1/projects/by-slug/p-1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.slug").value("p-1"));
        }

        @Test
        void updateProject_success() throws Exception {
                UpdateProjectRequest request = new UpdateProjectRequest("New", "Desc", null, null);
                ProjectResponse response = new ProjectResponse(
                                mockProjectId, "New", "new", "Desc",
                                ProjectVisibility.PRIVATE, ProjectStatus.ACTIVE,
                                mockUserId, OffsetDateTime.now(), OffsetDateTime.now());

                Mockito.when(projectService.updateProject(eq(mockProjectId), any(UpdateProjectRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(put("/api/v1/projects/{id}", mockProjectId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("New"));
        }

        @Test
        void deleteProject_success() throws Exception {
                mockMvc.perform(delete("/api/v1/projects/{id}", mockProjectId))
                                .andExpect(status().isNoContent());

                Mockito.verify(projectService).deleteProject(mockProjectId);
        }
}
