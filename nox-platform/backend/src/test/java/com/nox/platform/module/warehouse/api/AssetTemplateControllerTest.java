package com.nox.platform.module.warehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.service.AssetTemplateService;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AssetTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssetTemplateControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AssetTemplateService templateService;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private OrgMemberRepository orgMemberRepository;

        @MockBean
        private UserRepository userRepository;

        @Test
        void createBlockTemplate_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                AssetTemplateController.CreateBlockTemplateRequest request = new AssetTemplateController.CreateBlockTemplateRequest(
                                null, "Iron Block", "Strong", null, Map.of("health", 100), "1.0");

                BlockTemplate mockTemplate = BlockTemplate.builder()
                                .name("Iron Block")
                                .description("Strong")
                                .build();
                mockTemplate.setId(UUID.randomUUID());

                when(templateService.createBlockTemplate(eq(warehouseId), isNull(), eq("Iron Block"), eq("Strong"),
                                isNull(),
                                any(), eq("1.0")))
                                .thenReturn(mockTemplate);

                mockMvc.perform(post("/api/v1/warehouses/{warehouseId}/templates/blocks", warehouseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Iron Block"));
        }

        @Test
        void createInvaderDefinition_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                AssetTemplateController.CreateInvaderRequest request = new AssetTemplateController.CreateInvaderRequest(
                                null, "GOBLIN_1", "Goblin", "ENEMY", Map.of(), Map.of(), "v2");

                InvaderDefinition mockDef = InvaderDefinition.builder()
                                .code("GOBLIN_1")
                                .name("Goblin")
                                .build();

                when(templateService.createInvaderDefinition(eq(warehouseId), isNull(), eq("GOBLIN_1"), eq("Goblin"),
                                eq("ENEMY"), any(), any(), eq("v2")))
                                .thenReturn(mockDef);

                mockMvc.perform(post("/api/v1/warehouses/{warehouseId}/templates/invaders", warehouseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value("GOBLIN_1"))
                                .andExpect(jsonPath("$.name").value("Goblin"));
        }

        @Test
        void getBlockTemplates_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                BlockTemplate mockTemplate = BlockTemplate.builder().name("Block 1").build();

                when(templateService.getBlockTemplatesByWarehouse(warehouseId)).thenReturn(List.of(mockTemplate));

                mockMvc.perform(get("/api/v1/warehouses/{warehouseId}/templates/blocks", warehouseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Block 1"));
        }

        @Test
        void getInvaderDefinitions_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                InvaderDefinition mockDef = InvaderDefinition.builder().name("Invader 1").build();

                when(templateService.getInvaderDefinitionsByWarehouse(warehouseId)).thenReturn(List.of(mockDef));

                mockMvc.perform(get("/api/v1/warehouses/{warehouseId}/templates/invaders", warehouseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Invader 1"));
        }

        @Test
        void updateBlockTemplate_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                UUID templateId = UUID.randomUUID();

                AssetTemplateController.UpdateBlockTemplateRequest request = new AssetTemplateController.UpdateBlockTemplateRequest(
                                "Updated Block", null, null, null, null);

                BlockTemplate mockTemplate = BlockTemplate.builder().name("Updated Block").build();

                when(templateService.updateBlockTemplate(eq(warehouseId), eq(templateId), eq("Updated Block"), isNull(),
                                isNull(), isNull(), isNull()))
                                .thenReturn(mockTemplate);

                mockMvc.perform(put("/api/v1/warehouses/{warehouseId}/templates/blocks/{id}", warehouseId, templateId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Block"));
        }

        @Test
        void deleteInvaderDefinition_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                UUID invaderId = UUID.randomUUID();

                doNothing().when(templateService).deleteInvaderDefinition(warehouseId, invaderId);

                mockMvc.perform(delete("/api/v1/warehouses/{warehouseId}/templates/invaders/{id}", warehouseId,
                                invaderId))
                                .andExpect(status().isNoContent());

                verify(templateService).deleteInvaderDefinition(warehouseId, invaderId);
        }
}
