package com.nox.platform.module.engine.service;

import com.nox.platform.module.engine.api.request.CreateSnapshotRequest;
import com.nox.platform.module.engine.api.response.SnapshotResponse;
import com.nox.platform.module.engine.domain.CoreSnapshot;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.infrastructure.CoreSnapshotRepository;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EngineSnapshotService {

    private final CoreSnapshotRepository snapshotRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final TimeProvider timeProvider;

    @Transactional
    public SnapshotResponse saveDesignSnapshot(UUID projectId, CreateSnapshotRequest request, UUID currentUserId) {
        Project project = projectService.findProjectInternal(projectId);
        User user = userRepository.getReferenceById(currentUserId);

        CoreSnapshot snapshot = CoreSnapshot.create(
                project, request.name(), request.commitMessage(),
                request.fullStateDump(), user, timeProvider.now());

        snapshot = snapshotRepository.save(snapshot);
        return mapToResponse(snapshot);
    }

    @Transactional(readOnly = true)
    public List<SnapshotResponse> getProjectSnapshots(UUID projectId) {
        projectService.findProjectInternal(projectId);
        return snapshotRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JsonNode getSnapshotPayload(UUID projectId, UUID snapshotId) {
        projectService.findProjectInternal(projectId);

        CoreSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new DomainException("SNAPSHOT_NOT_FOUND", "Snapshot not found"));

        if (!snapshot.getProject().getId().equals(projectId)) {
            throw new DomainException("INVALID_SNAPSHOT_BOUNDS", "Mismatch mapping bounds");
        }

        return snapshot.getFullStateDump();
    }

    private SnapshotResponse mapToResponse(CoreSnapshot snapshot) {
        return new SnapshotResponse(
                snapshot.getId(),
                snapshot.getProject().getId(),
                snapshot.getName(),
                snapshot.getCommitMessage(),
                snapshot.getCreatedBy().getId(),
                snapshot.getCreatedAt());
    }
}

