package com.nox.platform.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.api.dto.BlockCreateRequest;
import com.nox.platform.api.dto.GraphResponse;
import com.nox.platform.core.engine.model.CoreBlock;
import com.nox.platform.core.engine.service.WorkspaceService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private WorkspaceService workspaceService;

        @MockBean
        private com.nox.platform.infra.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private com.nox.platform.infra.security.CustomUserDetailsService customUserDetailsService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void getGraph_ReturnsGraphResponse() throws Exception {
                UUID workspaceId = UUID.randomUUID();
                GraphResponse response = GraphResponse.builder()
                                .nodes(Collections.emptyList())
                                .edges(Collections.emptyList())
                                .build();

                when(workspaceService.getGraph(workspaceId)).thenReturn(response);

                mockMvc.perform(get("/api/workspaces/{workspaceId}/graph", workspaceId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nodes").isArray());
        }

        @Test
        void addBlock_ReturnsCreatedBlock() throws Exception {
                UUID workspaceId = UUID.randomUUID();
                BlockCreateRequest request = new BlockCreateRequest(UUID.randomUUID(), 100.0, 200.0);
                CoreBlock block = CoreBlock.builder()
                                .id(UUID.randomUUID())
                                .name("New Block")
                                .build();

                when(workspaceService.addBlock(eq(workspaceId), any(BlockCreateRequest.class))).thenReturn(block);

                mockMvc.perform(post("/api/workspaces/{workspaceId}/blocks", workspaceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("New Block"));
        }
}
