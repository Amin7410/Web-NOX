package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.api.dto.BlockTemplateResponse;
import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.service.BlockTemplateService;
import com.nox.platform.module.warehouse.service.command.CreateBlockTemplateCommand;
import com.nox.platform.module.warehouse.service.command.UpdateBlockTemplateCommand;
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
@RequestMapping("/api/v1/warehouses/{warehouseId}/templates/blocks")
@RequiredArgsConstructor
public class BlockTemplateController {

    private final BlockTemplateService blockTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<BlockTemplateResponse>> createBlockTemplate(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody CreateBlockTemplateRequest request) {
        
        CreateBlockTemplateCommand command = new CreateBlockTemplateCommand(
                warehouseId,
                request.collectionId(),
                request.name(),
                request.description(),
                request.thumbnailUrl(),
                request.structureData(),
                request.version()
        );

        BlockTemplate template = blockTemplateService.createBlockTemplate(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(BlockTemplateResponse.fromEntity(template)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BlockTemplateResponse>>> getBlockTemplates(@PathVariable UUID warehouseId) {
        List<BlockTemplateResponse> response = blockTemplateService.getBlockTemplatesByWarehouse(warehouseId).stream()
                .map(BlockTemplateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlockTemplateResponse>> updateBlockTemplate(
            @PathVariable UUID warehouseId,
            @PathVariable UUID id,
            @RequestBody UpdateBlockTemplateRequest request) {
        
        UpdateBlockTemplateCommand command = new UpdateBlockTemplateCommand(
                request.name(),
                request.description(),
                request.thumbnailUrl(),
                request.structureData(),
                request.version()
        );

        BlockTemplate template = blockTemplateService.updateBlockTemplate(warehouseId, id, command);
        return ResponseEntity.ok(ApiResponse.ok(BlockTemplateResponse.fromEntity(template)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlockTemplate(
            @PathVariable UUID warehouseId,
            @PathVariable UUID id) {
        blockTemplateService.deleteBlockTemplate(warehouseId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record CreateBlockTemplateRequest(
            UUID collectionId,
            @NotBlank String name,
            String description,
            String thumbnailUrl,
            @NotNull Map<String, Object> structureData,
            String version) {
    }

    public record UpdateBlockTemplateRequest(
            String name,
            String description,
            String thumbnailUrl,
            Map<String, Object> structureData,
            String version) {
    }
}
