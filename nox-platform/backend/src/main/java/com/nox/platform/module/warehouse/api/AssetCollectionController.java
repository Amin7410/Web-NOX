package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.service.AssetCollectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses/{warehouseId}/collections")
@RequiredArgsConstructor
public class AssetCollectionController {

    private final AssetCollectionService collectionService;

    @PostMapping
    public ResponseEntity<AssetCollection> createCollection(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody CreateCollectionRequest request) {

        AssetCollection collection = collectionService.createCollection(
                warehouseId,
                request.name(),
                request.parentCollectionId());
        return ResponseEntity.ok(collection);
    }

    @GetMapping
    public ResponseEntity<List<AssetCollection>> getRootCollections(@PathVariable UUID warehouseId) {
        return ResponseEntity.ok(collectionService.getRootCollections(warehouseId));
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<AssetCollection>> getChildCollections(
            @PathVariable UUID warehouseId,
            @PathVariable UUID parentId) {
        return ResponseEntity.ok(collectionService.getChildCollections(warehouseId, parentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetCollection> getCollection(
            @PathVariable("warehouseId") UUID warehouseId,
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(collectionService.getCollection(warehouseId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable("warehouseId") UUID warehouseId,
            @PathVariable("id") UUID id) {
        collectionService.deleteCollection(warehouseId, id);
        return ResponseEntity.noContent().build();
    }

    public record CreateCollectionRequest(
            @NotBlank String name,
            UUID parentCollectionId) {
    }

    @PutMapping("/{id}/parent")
    public ResponseEntity<AssetCollection> updateCollectionParent(
            @PathVariable UUID warehouseId,
            @PathVariable UUID id,
            @RequestBody UpdateCollectionParentRequest request) {
        return ResponseEntity.ok(collectionService.updateCollectionParent(warehouseId, id, request.newParentId()));
    }

    public record UpdateCollectionParentRequest(
            UUID newParentId) {
    }
}
