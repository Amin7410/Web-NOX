package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.api.dto.AssetCollectionResponse;
import com.nox.platform.module.warehouse.domain.AssetCollection;
import com.nox.platform.module.warehouse.service.AssetCollectionService;
import com.nox.platform.module.warehouse.service.command.CreateCollectionCommand;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouses/{warehouseId}/collections")
@RequiredArgsConstructor
public class AssetCollectionController {

    private final AssetCollectionService collectionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssetCollectionResponse>> createCollection(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody CreateCollectionRequest request) {

        CreateCollectionCommand command = new CreateCollectionCommand(
                warehouseId,
                request.name(),
                request.parentCollectionId()
        );

        AssetCollection collection = collectionService.createCollection(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(AssetCollectionResponse.fromEntity(collection)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetCollectionResponse>>> getRootCollections(@PathVariable UUID warehouseId) {
        List<AssetCollectionResponse> response = collectionService.getRootCollections(warehouseId).stream()
                .map(AssetCollectionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<AssetCollectionResponse>>> getChildCollections(
            @PathVariable UUID warehouseId,
            @PathVariable UUID parentId) {
        List<AssetCollectionResponse> response = collectionService.getChildCollections(warehouseId, parentId).stream()
                .map(AssetCollectionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetCollectionResponse>> getCollection(
            @PathVariable("warehouseId") UUID warehouseId,
            @PathVariable("id") UUID id) {
        AssetCollection collection = collectionService.getCollection(warehouseId, id);
        return ResponseEntity.ok(ApiResponse.ok(AssetCollectionResponse.fromEntity(collection)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(
            @PathVariable("warehouseId") UUID warehouseId,
            @PathVariable("id") UUID id) {
        collectionService.deleteCollection(warehouseId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/parent")
    public ResponseEntity<ApiResponse<AssetCollectionResponse>> updateCollectionParent(
            @PathVariable UUID warehouseId,
            @PathVariable UUID id,
            @RequestBody UpdateCollectionParentRequest request) {
        AssetCollection collection = collectionService.updateCollectionParent(warehouseId, id, request.newParentId());
        return ResponseEntity.ok(ApiResponse.ok(AssetCollectionResponse.fromEntity(collection)));
    }

    public record CreateCollectionRequest(
            @NotBlank String name,
            UUID parentCollectionId) {
    }

    public record UpdateCollectionParentRequest(
            UUID newParentId) {
    }
}
