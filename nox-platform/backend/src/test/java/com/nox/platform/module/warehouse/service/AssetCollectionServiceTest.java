package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.infrastructure.AssetCollectionRepository;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetCollectionServiceTest {

    @Mock
    private AssetCollectionRepository collectionRepository;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private AssetCollectionService collectionService;

    private UUID warehouseId;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouseId = UUID.randomUUID();
        warehouse = Warehouse.builder()
                .ownerId(UUID.randomUUID())
                .ownerType(OwnerType.USER)
                .name("Test Warehouse")
                .isSystem(false)
                .build();
        warehouse.setId(warehouseId);
    }

    @Test
    void createCollection_Success() {
        when(warehouseService.getWarehouseById(warehouseId)).thenReturn(warehouse);
        when(collectionRepository.findByWarehouseIdAndName(warehouseId, "Root Folder")).thenReturn(Optional.empty());
        when(collectionRepository.save(any(AssetCollection.class))).thenAnswer(i -> {
            AssetCollection coll = i.getArgument(0);
            coll.setId(UUID.randomUUID());
            return coll;
        });

        AssetCollection result = collectionService.createCollection(warehouseId, "Root Folder", null);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Root Folder");
        assertThat(result.getParentCollection()).isNull();
        verify(warehouseService).validateWriteOwnership(warehouse.getOwnerId(), warehouse.getOwnerType());
    }

    @Test
    void createCollection_WithParent_Success() {
        UUID parentId = UUID.randomUUID();
        AssetCollection parent = AssetCollection.builder().warehouse(warehouse).name("Parent").build();
        parent.setId(parentId);

        when(warehouseService.getWarehouseById(warehouseId)).thenReturn(warehouse);
        when(collectionRepository.findByWarehouseIdAndName(warehouseId, "Child")).thenReturn(Optional.empty());
        when(collectionRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(collectionRepository.save(any(AssetCollection.class))).thenAnswer(i -> i.getArgument(0));

        AssetCollection result = collectionService.createCollection(warehouseId, "Child", parentId);

        assertThat(result.getParentCollection()).isEqualTo(parent);
    }

    @Test
    void updateCollectionParent_DetectsCyclicDependency() {
        UUID grandParentId = UUID.randomUUID();
        AssetCollection grandParent = AssetCollection.builder().warehouse(warehouse).name("GrandParent").build();
        grandParent.setId(grandParentId);

        UUID parentId = UUID.randomUUID();
        AssetCollection parent = AssetCollection.builder().warehouse(warehouse).name("Parent")
                .parentCollection(grandParent).build();
        parent.setId(parentId);

        UUID childId = UUID.randomUUID();
        AssetCollection child = AssetCollection.builder().warehouse(warehouse).name("Child").parentCollection(parent)
                .build();
        child.setId(childId);

        // We want to set GrandParent's parent to Child, creating a cycle: GrandParent
        // -> Child -> Parent -> GrandParent
        when(collectionRepository.findById(grandParentId)).thenReturn(Optional.of(grandParent));
        when(collectionRepository.findById(childId)).thenReturn(Optional.of(child));

        // When validation walks up the tree from new parent (Child) it will look up
        // Child's parent (Parent)
        // Ensure child and parent are loaded when trace walks up
        when(collectionRepository.findById(childId)).thenReturn(Optional.of(child));
        when(collectionRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(collectionRepository.findById(grandParentId)).thenReturn(Optional.of(grandParent));

        assertThatThrownBy(() -> collectionService.updateCollectionParent(warehouseId, grandParentId, childId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("cyclic dependency");
    }

    @Test
    void deleteCollection_FailsIfChildrenExist() {
        UUID parentId = UUID.randomUUID();
        AssetCollection parent = AssetCollection.builder().warehouse(warehouse).name("Parent").build();
        parent.setId(parentId);

        when(collectionRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(collectionRepository.findByWarehouseIdAndParentCollectionId(warehouseId, parentId))
                .thenReturn(List.of(mock(AssetCollection.class))); // Returns 1 child

        assertThatThrownBy(() -> collectionService.deleteCollection(warehouseId, parentId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("child collections");
    }

    @Test
    void deleteCollection_SoftDeletesIfEmpty() {
        UUID folderId = UUID.randomUUID();
        AssetCollection folder = AssetCollection.builder().warehouse(warehouse).name("Folder").build();
        folder.setId(folderId);

        when(collectionRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(collectionRepository.findByWarehouseIdAndParentCollectionId(warehouseId, folderId))
                .thenReturn(List.of()); // No children

        collectionService.deleteCollection(warehouseId, folderId);

        assertThat(folder.getDeletedAt()).isNotNull();
        verify(collectionRepository).save(folder);
    }
}
