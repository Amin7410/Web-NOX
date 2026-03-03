package com.nox.platform.module.engine.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.engine.api.request.CreateSnapshotRequest;
import com.nox.platform.module.engine.api.response.SnapshotResponse;
import com.nox.platform.module.engine.service.EngineSnapshotService;
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

@WebMvcTest(SnapshotController.class)
@AutoConfigureMockMvc(addFilters = false)
class SnapshotControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private EngineSnapshotService snapshotService;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private OrgMemberRepository orgMemberRepository;

        @MockBean
        private JwtService jwtService;

        private UUID mockProjectId;
        private UUID mockSnapshotId;
        private UUID mockUserId;
        private MockedStatic<SecurityUtil> mockedSecurityUtil;

        @BeforeEach
        void setUp() {
                mockProjectId = UUID.randomUUID();
                mockSnapshotId = UUID.randomUUID();
                mockUserId = UUID.randomUUID();
                mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
                mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(mockUserId);
        }

        @AfterEach
        void tearDown() {
                mockedSecurityUtil.close();
        }

        @Test
        void commitDesignState_success() throws Exception {
                JsonNode dumpNode = objectMapper.readTree("{\"nodes\":[]}");
                CreateSnapshotRequest request = new CreateSnapshotRequest("Version 1", "Init", dumpNode);

                SnapshotResponse response = new SnapshotResponse(
                                mockSnapshotId, mockProjectId, "Version 1", "Init",
                                mockUserId, OffsetDateTime.now());

                Mockito.when(snapshotService.saveDesignSnapshot(eq(mockProjectId), any(CreateSnapshotRequest.class),
                                eq(mockUserId)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/v1/projects/{projectId}/snapshots/commit", mockProjectId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(mockSnapshotId.toString()))
                                .andExpect(jsonPath("$.name").value("Version 1"));
        }

        @Test
        void getProjectSnapshots_success() throws Exception {
                SnapshotResponse response = new SnapshotResponse(
                                mockSnapshotId, mockProjectId, "Version 1", "Init",
                                mockUserId, OffsetDateTime.now());

                Mockito.when(snapshotService.getProjectSnapshots(mockProjectId)).thenReturn(List.of(response));

                mockMvc.perform(get("/api/v1/projects/{projectId}/snapshots", mockProjectId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Version 1"));
        }

        @Test
        void getSnapshotPayload_success() throws Exception {
                JsonNode dumpNode = objectMapper.readTree("{\"nodes\":[{\"type\":\"root\"}]}");

                Mockito.when(snapshotService.getSnapshotPayload(mockProjectId, mockSnapshotId)).thenReturn(dumpNode);

                mockMvc.perform(get("/api/v1/projects/{projectId}/snapshots/{snapshotId}/payload", mockProjectId,
                                mockSnapshotId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nodes[0].type").value("root"));
        }
}
