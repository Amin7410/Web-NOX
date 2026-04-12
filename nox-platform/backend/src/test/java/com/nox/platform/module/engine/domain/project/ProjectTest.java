package com.nox.platform.module.engine.domain.project;

import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.domain.ProjectStatus;
import com.nox.platform.module.engine.domain.ProjectVisibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Project Domain Unit Tests")
class ProjectTest {

    @Test
    @DisplayName("Should update project metadata correctly")
    void shouldUpdateMetadata() {
        // Given
        Project project = Project.builder()
                .name("Old Name")
                .slug("old-name")
                .description("Old Desc")
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .build();

        // When
        project.updateMetadata("New Name", "new-name", "New Desc", ProjectVisibility.PUBLIC, ProjectStatus.ARCHIVED);

        // Then
        assertThat(project.getName()).isEqualTo("New Name");
        assertThat(project.getSlug()).isEqualTo("new-name");
        assertThat(project.getDescription()).isEqualTo("New Desc");
        assertThat(project.getVisibility()).isEqualTo(ProjectVisibility.PUBLIC);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    @DisplayName("Should soft delete project")
    void shouldSoftDelete() {
        // Given
        Project project = Project.builder().name("Test").build();
        OffsetDateTime now = OffsetDateTime.now();

        // When
        project.softDelete(now);

        // Then
        assertThat(project.getDeletedAt()).isEqualTo(now);
    }
}
