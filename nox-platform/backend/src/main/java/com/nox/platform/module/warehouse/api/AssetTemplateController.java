package com.nox.platform.module.warehouse.api;

import com.nox.platform.module.warehouse.domain.BlockTemplate;
import com.nox.platform.module.warehouse.domain.InvaderDefinition;
import com.nox.platform.module.warehouse.service.AssetTemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses/{warehouseId}/templates")
@RequiredArgsConstructor
public class AssetTemplateController {

        private final AssetTemplateService templateService;

        @PostMapping("/blocks")
        public ResponseEntity<BlockTemplate> createBlockTemplate(
                        @PathVariable UUID warehouseId,
                        @Valid @RequestBody CreateBlockTemplateRequest request) {
                BlockTemplate template = templateService.createBlockTemplate(
                                warehouseId,
                                request.collectionId(),
                                request.name(),
                                request.description(),
                                request.thumbnailUrl(),
                                request.structureData(),
                                request.version());
                return ResponseEntity.ok(template);
        }

        @GetMapping("/blocks")
        public ResponseEntity<List<BlockTemplate>> getBlockTemplates(@PathVariable UUID warehouseId) {
                return ResponseEntity.ok(templateService.getBlockTemplatesByWarehouse(warehouseId));
        }

        @PutMapping("/blocks/{id}")
        public ResponseEntity<BlockTemplate> updateBlockTemplate(
                        @PathVariable UUID warehouseId,
                        @PathVariable UUID id,
                        @RequestBody UpdateBlockTemplateRequest request) {
                BlockTemplate template = templateService.updateBlockTemplate(
                                warehouseId, id, request.name(), request.description(), request.thumbnailUrl(),
                                request.structureData(),
                                request.version());
                return ResponseEntity.ok(template);
        }

        @DeleteMapping("/blocks/{id}")
        public ResponseEntity<Void> deleteBlockTemplate(
                        @PathVariable UUID warehouseId,
                        @PathVariable UUID id) {
                templateService.deleteBlockTemplate(warehouseId, id);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/invaders")
        public ResponseEntity<InvaderDefinition> createInvaderDefinition(
                        @PathVariable UUID warehouseId,
                        @Valid @RequestBody CreateInvaderRequest request) {
                InvaderDefinition definition = templateService.createInvaderDefinition(
                                warehouseId,
                                request.collectionId(),
                                request.code(),
                                request.name(),
                                request.category(),
                                request.configSchema(),
                                request.compilerHooks(),
                                request.version());
                return ResponseEntity.ok(definition);
        }

        @GetMapping("/invaders")
        public ResponseEntity<List<InvaderDefinition>> getInvaderDefinitions(@PathVariable UUID warehouseId) {
                return ResponseEntity.ok(templateService.getInvaderDefinitionsByWarehouse(warehouseId));
        }

        @PutMapping("/invaders/{id}")
        public ResponseEntity<InvaderDefinition> updateInvaderDefinition(
                        @PathVariable UUID warehouseId,
                        @PathVariable UUID id,
                        @RequestBody UpdateInvaderRequest request) {
                InvaderDefinition definition = templateService.updateInvaderDefinition(
                                warehouseId, id, request.name(), request.category(), request.configSchema(),
                                request.compilerHooks(),
                                request.version());
                return ResponseEntity.ok(definition);
        }

        @DeleteMapping("/invaders/{id}")
        public ResponseEntity<Void> deleteInvaderDefinition(
                        @PathVariable UUID warehouseId,
                        @PathVariable UUID id) {
                templateService.deleteInvaderDefinition(warehouseId, id);
                return ResponseEntity.noContent().build();
        }

        public record CreateBlockTemplateRequest(
                        UUID collectionId,
                        @NotBlank String name,
                        String description,
                        String thumbnailUrl,
                        @NotNull Map<String, Object> structureData,
                        String version) {
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

        public record UpdateBlockTemplateRequest(
                        String name,
                        String description,
                        String thumbnailUrl,
                        Map<String, Object> structureData,
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
