# Test Specifications: Workspace

This folder contains unit tests for the `Workspace` domain entity, focusing on metadata consistency and operational status transitions within a project.

## 1. Metadata and Configuration
Workspaces define the working environment (e.g., Canvas, Logic, Frontend). These tests ensure that their configuration remains valid.

### WS-01: Metadata Update
This scenario validates that a workspace can have its name and type correctly modified. The system verifies that the update logic correctly reflects these changes in the entity state without affecting unrelated fields.
- **Status:** PASSED

---

## 2. Operational Transitions
Workspaces move through various lifecycle statuses as they are developed and published.

### WS-02: Status Transition
Workspaces typically start as `DRAFT` and can move to `PUBLISHED` or `ARCHIVED`. This test ensures that the `updateStatus` method correctly transitions the workspace state according to the domain rules.
- **Status:** PASSED

### WS-03: Soft Delete Execution
Like projects, workspaces utilize a soft-delete mechanism. This test verifies that performing a soft delete correctly populates the `deleted_at` field with the timestamp of the operation.
- **Status:** PASSED

---

## Technical Execution
The tests are implemented using JUnit 5 and AssertJ. To execute this suite independently:
```bash
./gradlew test --tests com.nox.platform.module.engine.domain.workspace.WorkspaceTest
```
