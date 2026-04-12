package com.nox.platform.module.engine.infrastructure;

import com.nox.platform.BaseIntegrationTest;
import com.nox.platform.module.engine.domain.CoreBlock;
import com.nox.platform.module.engine.domain.Project;
import com.nox.platform.module.engine.domain.Workspace;
import com.nox.platform.module.engine.domain.WorkspaceStatus;
import com.nox.platform.module.engine.domain.WorkspaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DisplayName("CoreBlockRepository Integration Tests (PostgreSQL)")
class CoreBlockRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CoreBlockRepository coreBlockRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private com.nox.platform.module.iam.infrastructure.UserRepository userRepository;

    @Autowired
    private com.nox.platform.module.tenant.infrastructure.OrganizationRepository organizationRepository;

    private Workspace workspace;
    private Project project;
    private com.nox.platform.module.iam.domain.User user;
    private com.nox.platform.module.tenant.domain.Organization organization;

    @BeforeEach
    void setUp() {
        organization = com.nox.platform.module.tenant.domain.Organization.builder()
                .name("Integration Test Org")
                .slug("test-org-" + UUID.randomUUID())
                .build();
        organization.initializeTimestamps(OffsetDateTime.now());
        organization = organizationRepository.save(organization);

        user = com.nox.platform.module.iam.domain.User.builder()
                .email("test-" + UUID.randomUUID() + "@example.com")
                .fullName("Test User")
                .build();
        user.initializeTimestamps(OffsetDateTime.now());
        user = userRepository.save(user);

        project = Project.builder()
                .organization(organization)
                .createdBy(user)
                .name("Test Project")
                .slug("test-project-" + UUID.randomUUID())
                .build();
        project.initializeTimestamps(OffsetDateTime.now());
        project = projectRepository.save(project);

        workspace = Workspace.builder()
                .project(project)
                .createdBy(user)
                .name("Test Workspace")
                .type(WorkspaceType.CANVAS)
                .status(WorkspaceStatus.DRAFT)
                .build();
        workspace.initializeTimestamps(OffsetDateTime.now());
        workspace = workspaceRepository.save(workspace);
    }

    @Test
    @DisplayName("Should successfully execute recursive CTE to find nested blocks")
    void shouldFindDescendantBlockIds() {
        // Given: Create a hierarchy Parent -> Child -> Grandchild
        CoreBlock parent = createBlock("Parent", null);
        CoreBlock child = createBlock("Child", parent);
        CoreBlock grandchild = createBlock("Grandchild", child);
        CoreBlock unrelated = createBlock("Unrelated", null);

        // When
        List<UUID> descendantIds = coreBlockRepository.findDescendantBlockIdsByRootId(parent.getId());

        // Then
        assertThat(descendantIds).hasSize(3);
        assertThat(descendantIds).containsExactlyInAnyOrder(parent.getId(), child.getId(), grandchild.getId());
        assertThat(descendantIds).doesNotContain(unrelated.getId());
    }

    @Test
    @DisplayName("Should correctly filter blocks by Workspace ID")
    void shouldFilterByWorkspace() {
        // Given
        createBlock("W1 Block", null);
        
        Workspace workspace2 = Workspace.builder()
                .project(project)
                .name("Workspace 2")
                .type(WorkspaceType.CANVAS)
                .status(WorkspaceStatus.DRAFT)
                .createdBy(user)
                .build();
        workspace2.initializeTimestamps(OffsetDateTime.now());
        workspace2 = workspaceRepository.save(workspace2);
        
        CoreBlock blockW2 = CoreBlock.builder()
                .workspace(workspace2)
                .name("W2 Block")
                .type("logic")
                .createdBy(user)
                .build();
        blockW2.initializeTimestamps(OffsetDateTime.now());
        coreBlockRepository.save(blockW2);

        // When
        List<CoreBlock> blocks = coreBlockRepository.findByWorkspaceId(workspace.getId());

        // Then
        assertThat(blocks).hasSize(1);
        assertThat(blocks.get(0).getName()).isEqualTo("W1 Block");
    }

    private CoreBlock createBlock(String name, CoreBlock parent) {
        CoreBlock block = CoreBlock.builder()
                .workspace(workspace)
                .parentBlock(parent)
                .name(name)
                .type("logic")
                .createdBy(user)
                .build();
        block.initializeTimestamps(OffsetDateTime.now());
        return coreBlockRepository.save(block);
    }
}
