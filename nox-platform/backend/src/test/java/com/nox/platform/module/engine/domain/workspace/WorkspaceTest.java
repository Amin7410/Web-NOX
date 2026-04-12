package com.nox.platform.module.engine.domain.workspace;

import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.domain.WorkspaceStatus;
import com.nox.platform.module.engine.domain.WorkspaceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Workspace Domain Unit Tests")
class WorkspaceTest {

    @Test
    @DisplayName("Should update workspace metadata correctly")
    void shouldUpdateMetadata() {
        // Given
        Workspace workspace = Workspace.builder()
                .name("Old Workspace")
                .type(WorkspaceType.MIXED)
                .build();

        // When
        workspace.updateMetadata("New Workspace", WorkspaceType.CANVAS);

        // Then
        assertThat(workspace.getName()).isEqualTo("New Workspace");
        assertThat(workspace.getType()).isEqualTo(WorkspaceType.CANVAS);
    }

    @Test
    @DisplayName("Should update workspace status")
    void shouldUpdateStatus() {
        // Given
        Workspace workspace = Workspace.builder().status(WorkspaceStatus.DRAFT).build();

        // When
        workspace.updateStatus(WorkspaceStatus.PUBLISHED);

        // Then
        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should soft delete workspace")
    void shouldSoftDelete() {
        // Given
        Workspace workspace = Workspace.builder().name("Test").build();
        OffsetDateTime now = OffsetDateTime.now();

        // When
        workspace.softDelete(now);

        // Then
        assertThat(workspace.getDeletedAt()).isEqualTo(now);
    }
}
