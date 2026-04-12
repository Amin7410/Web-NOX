package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.api.dto.InvaderDefinitionResponse;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.service.InvaderDefinitionService;
import com.nox.platform.module.warehouse.service.command.CreateInvaderDefinitionCommand;
import com.nox.platform.module.warehouse.service.command.UpdateInvaderDefinitionCommand;
import com.nox.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouses/{warehouseId}/templates/invaders")
@RequiredArgsConstructor
public class InvaderDefinitionController {

    private final InvaderDefinitionService invaderDefinitionService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvaderDefinitionResponse>> createInvaderDefinition(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody CreateInvaderRequest request) {
        
        CreateInvaderDefinitionCommand command = new CreateInvaderDefinitionCommand(
                warehouseId,
                request.collectionId(),
                request.code(),
                request.name(),
                request.category(),
                request.configSchema(),
                request.compilerHooks(),
                request.version()
        );

        InvaderDefinition definition = invaderDefinitionService.createInvaderDefinition(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(InvaderDefinitionResponse.fromEntity(definition)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvaderDefinitionResponse>>> getInvaderDefinitions(@PathVariable UUID warehouseId) {
        List<InvaderDefinitionResponse> response = invaderDefinitionService.getInvaderDefinitionsByWarehouse(warehouseId).stream()
                .map(InvaderDefinitionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvaderDefinitionResponse>> updateInvaderDefinition(
            @PathVariable UUID warehouseId,
            @PathVariable UUID id,
            @RequestBody UpdateInvaderRequest request) {
        
        UpdateInvaderDefinitionCommand command = new UpdateInvaderDefinitionCommand(
                request.name(),
                request.category(),
                request.configSchema(),
                request.compilerHooks(),
                request.version()
        );

        InvaderDefinition definition = invaderDefinitionService.updateInvaderDefinition(warehouseId, id, command);
        return ResponseEntity.ok(ApiResponse.ok(InvaderDefinitionResponse.fromEntity(definition)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvaderDefinition(
            @PathVariable UUID warehouseId,
            @PathVariable UUID id) {
        invaderDefinitionService.deleteInvaderDefinition(warehouseId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record CreateInvaderRequest(
            UUID collectionId,
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String category,
            @NotNull Map<String, Object> configSchema,
            @NotNull Map<String, Object> compilerHooks,
            String version) {
    }

    public record UpdateInvaderRequest(
            String name,
            String category,
            Map<String, Object> configSchema,
            Map<String, Object> compilerHooks,
            String version) {
    }
}
