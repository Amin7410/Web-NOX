package com.nox.platform.core.engine.service;

import com.nox.platform.api.dto.BlockCreateRequest;
import com.nox.platform.core.engine.model.CoreBlock;
import com.nox.platform.core.engine.model.Workspace;
import com.nox.platform.core.warehouse.model.BlockTemplate;
import com.nox.platform.infra.persistence.engine.CoreBlockRepository;
import com.nox.platform.infra.persistence.engine.CoreRelationRepository;
import com.nox.platform.infra.persistence.engine.WorkspaceRepository;
import com.nox.platform.infra.persistence.warehouse.BlockTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private CoreBlockRepository coreBlockRepository;
    @Mock
    private CoreRelationRepository coreRelationRepository;
    @Mock
    private BlockTemplateRepository blockTemplateRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private UUID workspaceId;
    private Workspace workspace;
    private BlockTemplate template;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder().id(workspaceId).name("Test Workspace").build();

        template = BlockTemplate.builder()
                .id(UUID.randomUUID())
                .name("Test Template")
                .structureData(Map.of("key", "value"))
                .build();
    }

    @Test
    void addBlock_Success() {
        // Arrange
        BlockCreateRequest request = new BlockCreateRequest();
        request.setTemplateId(template.getId());
        request.setX(100.0);
        request.setY(200.0);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(blockTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(coreBlockRepository.save(any(CoreBlock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CoreBlock result = workspaceService.addBlock(workspaceId, request);

        // Assert
        assertNotNull(result);
        assertEquals(template.getName(), result.getName());
        assertEquals("CANONICAL", result.getType());

        // Verify visual config mapping
        Map<String, Object> visual = result.getVisual();
        assertEquals(100.0, visual.get("x"));
        assertEquals(200.0, visual.get("y"));
    }
}
