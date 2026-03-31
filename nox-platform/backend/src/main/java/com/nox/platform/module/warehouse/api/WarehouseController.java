package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.domain.OwnerType;
import com.nox.platform.module.warehouse.domain.Warehouse;
import com.nox.platform.module.warehouse.service.WarehouseService;
import com.nox.platform.shared.api.ApiResponse;
import com.nox.platform.shared.util.SecurityUtil;
import com.nox.platform.shared.exception.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Warehouse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        UUID ownerId = request.ownerId();

        // If owner is a user, default to themselves.
        if (request.ownerType() == OwnerType.USER) {
            UUID currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new DomainException("UNAUTHORIZED", "User not authenticated", 401);
            }
            if (ownerId == null) {
                ownerId = currentUserId; // Auto assign if missing
            }
        }
        // At this point if it's ORG, it MUST have been provided in the DTO
        if (ownerId == null) {
            throw new DomainException("INVALID_REQUEST", "ownerId is required for Orgs", 400);
        }

        Warehouse warehouse = warehouseService.createWarehouse(
                ownerId,
                request.ownerType(),
                request.name(),
                request.isSystem());
        return ApiResponse.ok(warehouse);
    }

    @GetMapping("/{id}")
    public ApiResponse<Warehouse> getWarehouse(@PathVariable UUID id) {
        return ApiResponse.ok(warehouseService.getWarehouseById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Warehouse>> getWarehousesByOwner(@PathVariable UUID ownerId) {
        return ApiResponse.ok(warehouseService.getWarehousesByOwner(ownerId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouse(@PathVariable UUID id) {
        warehouseService.deleteWarehouse(id);
    }

    public record CreateWarehouseRequest(
            UUID ownerId, // Made optional. Fallback context for Users
            @NotNull OwnerType ownerType,
            @NotBlank String name,
            boolean isSystem) {
    }
}
