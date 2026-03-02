package com.nox.platform.module.warehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.service.WarehouseService;
import com.nox.platform.module.iam.infrastructure.security.JwtService;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WarehouseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WarehouseService warehouseService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private OrgMemberRepository orgMemberRepository;

    @MockBean
    private UserRepository userRepository;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        currentUserId = UUID.randomUUID();
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
    }

    @AfterEach
    void tearDown() {
        securityUtilMockedStatic.close();
    }

    @Test
    void createWarehouse_ImplicitUserOwner_Success() throws Exception {
        WarehouseController.CreateWarehouseRequest request = new WarehouseController.CreateWarehouseRequest(
                null, OwnerType.USER, "My Personal Vault", false);

        Warehouse mockWarehouse = Warehouse.builder()
                .ownerId(currentUserId)
                .ownerType(OwnerType.USER)
                .name("My Personal Vault")
                .isSystem(false)
                .build();
        mockWarehouse.setId(UUID.randomUUID());

        when(warehouseService.createWarehouse(eq(currentUserId), eq(OwnerType.USER), eq("My Personal Vault"),
                eq(false)))
                .thenReturn(mockWarehouse);

        mockMvc.perform(post("/api/v1/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Personal Vault"))
                .andExpect(jsonPath("$.ownerType").value("USER"));
    }

    @Test
    void createWarehouse_ExplicitOrgOwner_Success() throws Exception {
        UUID orgId = UUID.randomUUID();
        WarehouseController.CreateWarehouseRequest request = new WarehouseController.CreateWarehouseRequest(
                orgId, OwnerType.ORG, "Org Vault", false);

        Warehouse mockWarehouse = Warehouse.builder()
                .ownerId(orgId)
                .ownerType(OwnerType.ORG)
                .name("Org Vault")
                .isSystem(false)
                .build();
        mockWarehouse.setId(UUID.randomUUID());

        when(warehouseService.createWarehouse(eq(orgId), eq(OwnerType.ORG), eq("Org Vault"), eq(false)))
                .thenReturn(mockWarehouse);

        mockMvc.perform(post("/api/v1/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Org Vault"))
                .andExpect(jsonPath("$.ownerId").value(orgId.toString()));
    }

    @Test
    void createWarehouse_MissingOrgOwnerId_ThrowsException() throws Exception {
        WarehouseController.CreateWarehouseRequest request = new WarehouseController.CreateWarehouseRequest(
                null, OwnerType.ORG, "Org Vault", false);

        mockMvc.perform(post("/api/v1/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Depending on global exception handler, could be 400
    }

    @Test
    void getWarehouse_Success() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        Warehouse mockWarehouse = Warehouse.builder().name("Vault").ownerType(OwnerType.USER).build();
        mockWarehouse.setId(warehouseId);

        when(warehouseService.getWarehouseById(warehouseId)).thenReturn(mockWarehouse);

        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(warehouseId.toString()))
                .andExpect(jsonPath("$.name").value("Vault"));
    }

    @Test
    void getWarehousesByOwner_Success() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Warehouse mockWarehouse = Warehouse.builder().name("Vault 1").build();
        mockWarehouse.setId(UUID.randomUUID());

        when(warehouseService.getWarehousesByOwner(ownerId)).thenReturn(List.of(mockWarehouse));

        mockMvc.perform(get("/api/v1/warehouses/owner/{ownerId}", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vault 1"));
    }

    @Test
    void deleteWarehouse_Success() throws Exception {
        UUID warehouseId = UUID.randomUUID();

        // Testing successful void return
        doNothing().when(warehouseService).deleteWarehouse(warehouseId);

        mockMvc.perform(delete("/api/v1/warehouses/{id}", warehouseId))
                .andExpect(status().isNoContent());

        verify(warehouseService).deleteWarehouse(warehouseId);
    }
}
