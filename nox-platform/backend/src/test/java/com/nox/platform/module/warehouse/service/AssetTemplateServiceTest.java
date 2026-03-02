package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.BlockTemplateRepository;
import com.nox.platform.module.warehouse.infrastructure.InvaderDefinitionRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetTemplateServiceTest {

    @Mock
    private BlockTemplateRepository blockTemplateRepository;

    @Mock
    private InvaderDefinitionRepository invaderDefinitionRepository;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private AssetCollectionService collectionService;

    @InjectMocks
    private AssetTemplateService templateService;

    private UUID warehouseId;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouseId = UUID.randomUUID();
        warehouse = Warehouse.builder()
                .ownerId(UUID.randomUUID())
                .ownerType(OwnerType.USER)
                .name("Template Warehouse")
                .isSystem(false)
                .build();
        warehouse.setId(warehouseId);
    }

    @Test
    void createBlockTemplate_Success() {
        when(warehouseService.getWarehouseById(warehouseId)).thenReturn(warehouse);
        when(blockTemplateRepository.save(any(BlockTemplate.class))).thenAnswer(i -> {
            BlockTemplate template = i.getArgument(0);
            template.setId(UUID.randomUUID());
            return template;
        });

        BlockTemplate result = templateService.createBlockTemplate(warehouseId, null, "Sword", "description", null,
                Map.of("damage", 10), "1.0");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sword");
        verify(warehouseService).validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
    }

    @Test
    void createInvaderDefinition_UniqueCodeEnforced() {
        when(warehouseService.getWarehouseById(warehouseId)).thenReturn(warehouse);
        when(invaderDefinitionRepository.findByWarehouseIdAndCode(warehouseId, "DUPLICATE"))
                .thenReturn(Optional.of(mock(InvaderDefinition.class)));

        assertThatThrownBy(() -> templateService.createInvaderDefinition(
                warehouseId, null, "DUPLICATE", "Goblin", "AI_GOBLIN", null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateBlockTemplate_ValidatesOwnership() {
        UUID templateId = UUID.randomUUID();
        BlockTemplate template = BlockTemplate.builder().warehouse(warehouse).name("A").build();
        template.setId(templateId);

        when(blockTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(blockTemplateRepository.save(any(BlockTemplate.class))).thenAnswer(i -> i.getArgument(0));

        // This will simulate successful validation silently
        doNothing().when(warehouseService).validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());

        BlockTemplate result = templateService.updateBlockTemplate(warehouseId, templateId, "New Name", null, null,
                null, null);
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void updateBlockTemplate_InDifferentWarehouse_ThrowsException() {
        UUID templateId = UUID.randomUUID();
        Warehouse wrongWarehouse = Warehouse.builder().name("Wrong").build();
        wrongWarehouse.setId(UUID.randomUUID());

        BlockTemplate template = BlockTemplate.builder().warehouse(wrongWarehouse).name("A").build();
        template.setId(templateId);

        when(blockTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThatThrownBy(
                () -> templateService.updateBlockTemplate(warehouseId, templateId, "B", null, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("does not belong to the specified warehouse");
    }
}
