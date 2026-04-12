# Test Specifications: CoreBlock

This directory contains comprehensive unit tests for the `CoreBlock` domain entity. The tests are designed to validate the internal state consistency, concurrent locking mechanisms, and hierarchical integrity of the block system.

## 1. Locking Mechanisms
The following scenarios ensure that blocks can be safely modified in the NOX Studio's collaborative environment.

### CB-01: Valid Block Locking
In a standard scenario where a block has no active lock, a user should be able to acquire it. The system must verify that the block's `lockedBy` and `lockedAt` fields are correctly populated with the user's identity and the current timestamp.
- **Status:** PASSED

### CB-02: Expired Lock Overwrite
To prevent deadlocks from abandoned sessions, a lock automatically expires after 2 minutes. This test verifies that if User A holds an expired lock, User B is permitted to overwrite it and take control of the block.
- **Status:** PASSED

### CB-03: Lock Conflict Handling
When a block is actively locked by a user (less than 2 minutes ago), other users must be prohibited from acquiring it. The system is expected to throw a `DomainException` with the code `BLOCK_LOCKED`.
- **Status:** PASSED

### CB-04: Valid Unlock
A user holding a valid lock must be able to release it voluntarily. Upon unlocking, the block's locking metadata (`lockedBy`, `lockedAt`) must be nullified to allow other users access.
- **Status:** PASSED

### CB-05: Unauthorized Unlock
To ensure security, only the current lock holder is authorized to release a lock. If another user attempts to unlock the block, the system must reject the request and throw a `BLOCK_LOCKED` exception.
- **Status:** PASSED

---

## 2. Content Management
These tests ensure that block data can only be modified under appropriate conditions.

### CB-06: Update Unlocked Block
A block that is not currently locked by anyone is considered open for modification. Any valid update request should succeed in synchronizing the block's metadata and configuration data.
- **Status:** PASSED

### CB-07: Update Owned Lock
The user who successfully holds the current lock must be able to update the block's visual properties and technical configuration. The system verifies that these changes are reflected in the entity state.
- **Status:** PASSED

### CB-08: Blocked Update Prevention
Any attempt to update a block that is currently locked by another user must be blocked by the domain logic. This maintains data integrity across concurrent sessions by throwing a `BLOCK_LOCKED` exception.
- **Status:** PASSED

---

## 3. Hierarchical Integrity
These tests validate the tree-like structure of the blocks and prevent invalid states.

### CB-09: Valid Parent Assignment
Moving a block from one parent to another (or from root to a parent) must correctly update the internal parent-child relationship.
- **Status:** PASSED

### CB-10: Root Level Migration
A block can be moved to the root level by setting its parent to `null`. The system verifies that the reference is correctly cleared.
- **Status:** PASSED

### CB-11: Self-Assignment Prevention
A block cannot be its own parent. The system must detect this attempt and throw a `CIRCULAR_DEPENDENCY` exception to prevent corrupted states.
- **Status:** PASSED

### CB-12: Circular Dependency Detection
In a deeper hierarchy (e.g., A is father of B), B cannot become the father of A. The system must trace the lineage and throw a `CIRCULAR_DEPENDENCY` error if a cycle is detected.
- **Status:** PASSED

### CB-13: Hierarchy Depth Limit
To protect system performance and simplify rendering, the block hierarchy is limited to a depth of 10. Any attempt to create a deeper chain must be rejected with a `MAX_DEPTH_REACHED` exception.
- **Status:** PASSED

---

## Technical Execution
The tests are implemented using JUnit 5 and AssertJ. To execute this suite independently:
```bash
./gradlew test --tests com.nox.platform.module.engine.domain.coreblock.CoreBlockTest
```
