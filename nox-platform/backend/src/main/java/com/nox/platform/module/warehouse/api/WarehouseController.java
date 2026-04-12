package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.api.dto.WarehouseResponse;
import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.service.WarehouseService;
import com.nox.platform.shared.abstraction.SecurityProvider;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.exception.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final SecurityProvider securityProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        UUID ownerId = request.ownerId();

        if (ownerId == null && request.ownerType() == OwnerType.USER) {
            ownerId = securityProvider.getCurrentUserId()
                    .orElseThrow(() -> new DomainException("UNAUTHORIZED", "Authentication required"));
        }

        if (ownerId == null) {
            throw new DomainException("INVALID_REQUEST", "ownerId is required");
        }

        Warehouse warehouse = warehouseService.createWarehouse(
                ownerId,
                request.ownerType(),
                request.name(),
                request.isSystem());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(WarehouseResponse.fromEntity(warehouse)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouse(@PathVariable UUID id) {
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(ApiResponse.ok(WarehouseResponse.fromEntity(warehouse)));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getWarehousesByOwner(@PathVariable UUID ownerId) {
        List<WarehouseResponse> response = warehouseService.getWarehousesByOwner(ownerId).stream()
                .map(WarehouseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable UUID id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record CreateWarehouseRequest(
            UUID ownerId,
            @NotNull OwnerType ownerType,
            @NotBlank String name,
            boolean isSystem) {
    }
}

