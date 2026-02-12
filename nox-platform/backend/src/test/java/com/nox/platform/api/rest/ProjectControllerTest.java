package com.nox.platform.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.api.dto.ProjectCreateRequest;
import com.nox.platform.core.engine.model.Project;
import com.nox.platform.core.engine.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit test
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private com.nox.platform.infra.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.nox.platform.infra.security.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProject_ReturnsCreatedProject() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest("Test Project", "test-project", "Desc", null);
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .slug("test-project")
                .build();

        when(projectService.createProject(any(ProjectCreateRequest.class))).thenReturn(project);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void getProjects_ReturnsList() throws Exception {
        when(projectService.getAllProjects()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
