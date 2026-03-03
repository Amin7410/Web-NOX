package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateSnapshotRequest;
import com.nox.platform.module.engine.domain.CoreSnapshot;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.infrastructure.CoreSnapshotRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EngineSnapshotServiceTest {

    @Mock
    private CoreSnapshotRepository snapshotRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EngineSnapshotService snapshotService;

    private UUID mockProjectId;
    private UUID mockUserId;
    private User mockUser;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProjectId = UUID.randomUUID();
        mockUserId = UUID.randomUUID();
        mockUser = User.builder().id(mockUserId).build();
        mockProject = Project.builder().id(mockProjectId).build();
    }

    @Test
    void saveDesignSnapshot_withValidTenant_persistsPayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dumpNode = mapper.readTree("{\"nodes\": []}");
        CreateSnapshotRequest request = new CreateSnapshotRequest("Version 1 (Initial Build)", "feat: init layout",
                dumpNode);

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);
        when(userRepository.getReferenceById(mockUserId)).thenReturn(mockUser);

        CoreSnapshot savedSnap = CoreSnapshot.builder()
                .id(UUID.randomUUID())
                .name("Version 1 (Initial Build)")
                .project(mockProject)
                .createdBy(mockUser)
                .build();
        when(snapshotRepository.save(any())).thenReturn(savedSnap);

        var result = snapshotService.saveDesignSnapshot(mockProjectId, request, mockUserId);

        assertEquals("Version 1 (Initial Build)", result.name());
    }

    @Test
    void getProjectSnapshots_success() {
        CoreSnapshot snap = CoreSnapshot.builder().id(UUID.randomUUID()).name("V1").project(mockProject)
                .createdBy(mockUser).build();

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);
        when(snapshotRepository.findByProjectIdOrderByCreatedAtDesc(mockProjectId)).thenReturn(List.of(snap));

        var results = snapshotService.getProjectSnapshots(mockProjectId);

        assertEquals(1, results.size());
    }

    @Test
    void getSnapshotPayload_withMismatchedIds_throwsException() {
        UUID querySnapshotUuid = UUID.randomUUID();
        UUID otherProjectUuid = UUID.randomUUID(); // Doesn't match `mockProjectId`

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);

        Project siblingProject = Project.builder().id(otherProjectUuid).build();
        CoreSnapshot foreignSnap = CoreSnapshot.builder()
                .id(querySnapshotUuid)
                .project(siblingProject)
                .build();
        when(snapshotRepository.findById(querySnapshotUuid)).thenReturn(Optional.of(foreignSnap));

        DomainException exception = assertThrows(DomainException.class,
                () -> snapshotService.getSnapshotPayload(mockProjectId, querySnapshotUuid));

        assertEquals("INVALID_SNAPSHOT_BOUNDS", exception.getCode());
    }

    @Test
    void getSnapshotPayload_notFound_throwsException() {
        UUID querySnapshotUuid = UUID.randomUUID();

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);
        when(snapshotRepository.findById(querySnapshotUuid)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> snapshotService.getSnapshotPayload(mockProjectId, querySnapshotUuid));

        assertEquals("SNAPSHOT_NOT_FOUND", exception.getCode());
    }

    @Test
    void getSnapshotPayload_success() throws Exception {
        UUID querySnapshotUuid = UUID.randomUUID();

        when(projectService.findProjectInternal(mockProjectId)).thenReturn(mockProject);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode dumpNode = mapper.readTree("{\"nodes\":[]}");

        CoreSnapshot validSnap = CoreSnapshot.builder()
                .id(querySnapshotUuid)
                .project(mockProject)
                .fullStateDump(dumpNode)
                .build();
        when(snapshotRepository.findById(querySnapshotUuid)).thenReturn(Optional.of(validSnap));

        JsonNode payload = snapshotService.getSnapshotPayload(mockProjectId, querySnapshotUuid);

        assertEquals(dumpNode, payload);
    }
}
