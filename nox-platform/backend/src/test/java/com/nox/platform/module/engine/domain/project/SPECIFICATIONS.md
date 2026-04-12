# Test Specifications: Project

This folder contains unit tests for the `Project` domain entity, ensuring the integrity of metadata synchronization and lifecycle management.

## 1. Metadata Management
Projects are central entities that hold configuration for various workspaces. These tests ensure that the data remains consistent during updates.

### PRJ-01: Metadata Synchronisation
When a project's name, slug, or visibility is updated, the internal entity state must be refreshed accurately. This test verifies that all provided fields (Name, Slug, Description, Visibility, and Status) are correctly synchronized through the `updateMetadata` method.
- **Status:** PASSED

---

## 2. Lifecycle Management
Project deletion in NOX follows a soft-delete pattern to allow for potential recovery and data auditing.

### PRJ-02: Soft Delete Execution
This scenario validates the soft-delete mechanism. When a delete command is issued, the system must populate the `deleted_at` field with the current execution timestamp instead of physically removing the record from the database.
- **Status:** PASSED

---

## Technical Execution
The tests are implemented using JUnit 5 and AssertJ. To execute this suite independently:
```bash
./gradlew test --tests com.nox.platform.module.engine.domain.project.ProjectTest
```
