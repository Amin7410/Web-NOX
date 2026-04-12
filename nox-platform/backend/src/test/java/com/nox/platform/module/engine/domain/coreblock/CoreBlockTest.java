package com.nox.platform.module.engine.domain.coreblock;

import com.nox.platform.module.engine.domain.CoreBlock;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CoreBlock Domain Unit Tests")
class CoreBlockTest {

    private CoreBlock block;
    private User creator;
    private UUID userId;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        creator = User.builder().id(userId).email("test@nox.platform").build();
        now = OffsetDateTime.now();

        block = CoreBlock.builder()
                .id(UUID.randomUUID())
                .name("Test Block")
                .type("test-type")
                .createdBy(creator)
                .build();
    }

    @Nested
    @DisplayName("Locking Logic Tests")
    class LockingTests {

        @Test
        @DisplayName("Should successfully lock an unlocked block")
        void shouldLockUnlockedBlock() {
            // When
            block.lock(userId, now);

            // Then
            assertThat(block.getLockedBy()).isEqualTo(userId);
            assertThat(block.getLockedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should allow locking if the lock is expired (older than 2 minutes)")
        void shouldLockIfPreviousLockExpired() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            OffsetDateTime oldTime = now.minusMinutes(3);
            block.lock(otherUserId, oldTime);

            // When
            block.lock(userId, now);

            // Then
            assertThat(block.getLockedBy()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw exception when locking a block currently locked by another user")
        void shouldThrowExceptionWhenLockedByOther() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            OffsetDateTime recentTime = now.minusMinutes(1);
            block.lock(otherUserId, recentTime);

            // When & Then
            assertThatThrownBy(() -> block.lock(userId, now))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "BLOCK_LOCKED");
        }

        @Test
        @DisplayName("Should successfully unlock a block locked by the same user")
        void shouldUnlockBySameUser() {
            // Given
            block.lock(userId, now);

            // When
            block.unlock(userId, now.plusSeconds(30));

            // Then
            assertThat(block.getLockedBy()).isNull();
            assertThat(block.getLockedAt()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when unlocking a block locked by someone else")
        void shouldThrowExceptionWhenUnlockingOthersLock() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            block.lock(otherUserId, now);

            // When & Then
            assertThatThrownBy(() -> block.unlock(userId, now.plusSeconds(10)))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "BLOCK_LOCKED");
        }
    }

    @Nested
    @DisplayName("Content Update Tests")
    class ContentUpdateTests {

        @Test
        @DisplayName("Should update content when not locked")
        void shouldUpdateContentWhenNotLocked() {
            // Given
            Map<String, Object> newConfig = Map.of("key", "value");

            // When
            block.updateContent("New Name", newConfig, null, userId, now);

            // Then
            assertThat(block.getName()).isEqualTo("New Name");
            assertThat(block.getConfig()).isEqualTo(newConfig);
        }

        @Test
        @DisplayName("Should update content when locked by the same user")
        void shouldUpdateContentWhenLockedBySameUser() {
            // Given
            block.lock(userId, now.minusSeconds(10));
            Map<String, Object> newVisual = Map.of("color", "red");

            // When
            block.updateContent(null, null, newVisual, userId, now);

            // Then
            assertThat(block.getVisual()).isEqualTo(newVisual);
        }

        @Test
        @DisplayName("Should throw exception when updating content locked by another user")
        void shouldThrowWhenUpdatingLockedByOther() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            block.lock(otherUserId, now.minusSeconds(10));

            // When & Then
            assertThatThrownBy(() -> block.updateContent("New Name", null, null, userId, now))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "BLOCK_LOCKED");
        }
    }

    @Nested
    @DisplayName("Hierarchy & Circular Dependency Tests")
    class HierarchyTests {

        @Test
        @DisplayName("Should successfully move a block to a new parent")
        void shouldMoveToNewParent() {
            // Given
            CoreBlock newParent = CoreBlock.builder().id(UUID.randomUUID()).build();

            // When
            block.moveTo(newParent);

            // Then
            assertThat(block.getParentBlock()).isEqualTo(newParent);
        }

        @Test
        @DisplayName("Should allow moving to null parent (root level)")
        void shouldAllowMovingToNullParent() {
            // When
            block.moveTo(null);

            // Then
            assertThat(block.getParentBlock()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when block is its own parent")
        void shouldThrowWhenSelfParent() {
            // When & Then
            assertThatThrownBy(() -> block.moveTo(block))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "CIRCULAR_DEPENDENCY");
        }

        @Test
        @DisplayName("Should detect circular dependency in deeper hierarchy")
        void shouldDetectDeepCircularDependency() {
            // Given: block -> parent1 -> parent2
            UUID parent2Id = UUID.randomUUID();
            CoreBlock parent2 = CoreBlock.builder().id(parent2Id).build();
            
            CoreBlock parent1 = CoreBlock.builder()
                    .id(UUID.randomUUID())
                    .parentBlock(parent2)
                    .build();
            
            block.moveTo(parent1);

            // When & Then: Try to move parent2 to be a child of 'block'
            assertThatThrownBy(() -> parent2.moveTo(block))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "CIRCULAR_DEPENDENCY");
        }

        @Test
        @DisplayName("Should throw exception when maximum hierarchy depth is exceeded")
        void shouldThrowWhenMaxDepthExceeded() {
            // Given: Create a chain of 11 blocks (exceeding depth 10)
            CoreBlock current = CoreBlock.builder().id(UUID.randomUUID()).build();
            for (int i = 0; i < 11; i++) {
                CoreBlock child = CoreBlock.builder()
                        .id(UUID.randomUUID())
                        .parentBlock(current)
                        .build();
                current = child;
            }

            // 'current' is now at depth 11. Adding one more should fail.
            final CoreBlock lastParent = current;
            CoreBlock overlyDeepChild = CoreBlock.builder().id(UUID.randomUUID()).build();

            // When & Then
            assertThatThrownBy(() -> overlyDeepChild.moveTo(lastParent))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("code", "MAX_DEPTH_REACHED");
        }
    }
}
