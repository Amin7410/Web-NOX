package com.nox.platform.module.warehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.service.AssetCollectionService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AssetCollectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssetCollectionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AssetCollectionService collectionService;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private OrgMemberRepository orgMemberRepository;

        @MockBean
        private UserRepository userRepository;

        @Test
        void createCollection_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                AssetCollectionController.CreateCollectionRequest request = new AssetCollectionController.CreateCollectionRequest(
                                "My Collection", null);

                AssetCollection mockCollection = AssetCollection.builder()
                                .name("My Collection")
                                .warehouse(Warehouse.builder().build())
                                .build();
                mockCollection.setId(UUID.randomUUID());

                when(collectionService.createCollection(eq(warehouseId), eq("My Collection"), isNull()))
                                .thenReturn(mockCollection);

                mockMvc.perform(post("/api/v1/warehouses/{warehouseId}/collections", warehouseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("My Collection"));
        }

        @Test
        void getRootCollections_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                AssetCollection mockCollection = AssetCollection.builder().name("Root").build();

                when(collectionService.getRootCollections(warehouseId)).thenReturn(List.of(mockCollection));

                mockMvc.perform(get("/api/v1/warehouses/{warehouseId}/collections", warehouseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Root"));
        }

        @Test
        void updateCollectionParent_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                UUID collectionId = UUID.randomUUID();
                UUID newParentId = UUID.randomUUID();

                AssetCollectionController.UpdateCollectionParentRequest request = new AssetCollectionController.UpdateCollectionParentRequest(
                                newParentId);

                AssetCollection mockCollection = AssetCollection.builder().name("Moved").build();
                when(collectionService.updateCollectionParent(warehouseId, collectionId, newParentId))
                                .thenReturn(mockCollection);

                mockMvc.perform(put("/api/v1/warehouses/{warehouseId}/collections/{id}/parent", warehouseId,
                                collectionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Moved"));
        }

        @Test
        void deleteCollection_Success() throws Exception {
                UUID warehouseId = UUID.randomUUID();
                UUID collectionId = UUID.randomUUID();

                doNothing().when(collectionService).deleteCollection(warehouseId, collectionId);

                mockMvc.perform(delete("/api/v1/warehouses/{warehouseId}/collections/{id}", warehouseId, collectionId))
                                .andExpect(status().isNoContent());

                verify(collectionService).deleteCollection(warehouseId, collectionId);
        }
}
