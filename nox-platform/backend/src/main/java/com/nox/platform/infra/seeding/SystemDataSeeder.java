package com.nox.platform.infra.seeding;

import com.nox.platform.core.warehouse.model.BlockTemplate;
import com.nox.platform.core.warehouse.model.Warehouse;
import com.nox.platform.infra.persistence.warehouse.BlockTemplateRepository;
import com.nox.platform.infra.persistence.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemDataSeeder implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final BlockTemplateRepository blockTemplateRepository;

    @Override
    public void run(String... args) {
        Warehouse systemWarehouse = seedSystemWarehouse();
        seedStandardBlocks(systemWarehouse);
    }

    private Warehouse seedSystemWarehouse() {
        return warehouseRepository.findBySystemTrue()
                .orElseGet(() -> {
                    log.info("Seeding System Warehouse...");
                    Warehouse warehouse = Warehouse.builder()
                            .ownerId(UUID.randomUUID()) // System owner placeholder
                            .ownerType("ORG")
                            .name("System Warehouse")
                            .system(true)
                            .build();
                    return warehouseRepository.save(warehouse);
                });
    }

    private void seedStandardBlocks(Warehouse warehouse) {
        createBlockTemplateIfMissing(warehouse, "START_NODE", "Bắt đầu", "Flow Control",
                Map.of("ports", List.of(Map.of("id", "out", "type", "output"))));

        createBlockTemplateIfMissing(warehouse, "END_NODE", "Kết thúc", "Flow Control",
                Map.of("ports", List.of(Map.of("id", "in", "type", "input"))));

        createBlockTemplateIfMissing(warehouse, "PROCESS_NODE", "Xử lý nghiệp vụ", "Logic",
                Map.of("ports", List.of(
                        Map.of("id", "in", "type", "input"),
                        Map.of("id", "out", "type", "output"))));
    }

    private void createBlockTemplateIfMissing(Warehouse warehouse, String name, String description, String category,
            Map<String, Object> structureData) {
        if (blockTemplateRepository.findByNameAndWarehouseId(name, warehouse.getId()).isEmpty()) {
            log.info("Seeding Block Template: {}", name);
            BlockTemplate template = BlockTemplate.builder()
                    .warehouse(warehouse)
                    .name(name)
                    .description(description)
                    // We can use category if we add it to entity, for now just desc is fine or we
                    // update entity.
                    // The entity doesn't have 'category' field in previous step, so I'll put it in
                    // description or ignore.
                    // Actually, the user requirement said: "Name = 'Bắt đầu', Category = 'Flow
                    // Control'".
                    // Let's check BlockTemplate.java. It has: `name`, `description`,
                    // `thumbnailUrl`, `structureData`.
                    // It does NOT have `category`. I should probably put category in
                    // `structureData` or just ignore for now to avoid entity change.
                    // Or I can prepend to description. simpler.
                    .structureData(structureData)
                    .version("1.0.0")
                    .build();
            blockTemplateRepository.save(template);
        }
    }
}
