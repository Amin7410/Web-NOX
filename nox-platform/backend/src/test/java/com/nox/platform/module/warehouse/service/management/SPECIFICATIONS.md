# Test Specifications: Warehouse Management

This directory contains unit tests for the `WarehouseService`, which handles the lifecycle and access control for system and user warehouses.

## 1. Creation and Ownership Validations
The warehouse creation logic is protected by strict ownership checks to ensure data isolation.

### WH-01: Personal Warehouse Lifecycle
When a user attempts to create a personal warehouse for themselves, the system must validate the ownership and persist the new warehouse entity. This ensures that every user has a dedicated storage space in the NOX Platform.
- **Status:** PASSED

### WH-02: Unauthorized Creation Prevention
The system must prohibit a user from creating a warehouse for another user entity. Any such attempt is caught by the `validateWriteOwnership` check, and a `FORBIDDEN` exception is raised.
- **Status:** PASSED

### WH-03: Duplicate Warehouse Prevention
To maintain a 1:1 relationship between an owner and their primary warehouse, the system rejects creation requests if a warehouse already exists for the given owner ID and type.
- **Status:** PASSED

---

## 2. Organization and RBAC Scenarios
Access to organization-owned warehouses depends on the member's role and associated permissions.

### WH-04: Authorized Org Access
A member of an organization with the `workspace:manage` permission must be allowed to create or modify the organization's warehouse. The test verifies that the `orgMemberRepository` is correctly consulted during the RBAC check.
- **Status:** PASSED

### WH-05: Insufficient Permission Handling
If an organization member attempts an action without the required permissions (e.g., a Guest trying to manage the warehouse), the system must reject the request with a `FORBIDDEN` error.
- **Status:** PASSED

---

## 3. Deletion and Resource Cleanup
Warehouse deletion follows a soft-delete strategy with cascading effects on child resources.

### WH-06: Cascading Soft Delete
When a warehouse is deleted, the system marks the warehouse itself as deleted and triggers a cascading soft-delete for all associated `BlockTemplates` and `InvaderDefinitions`. This ensures that no orphan resources remain active.
- **Status:** PASSED

### WH-07: System Warehouse Protection
Crucial system-level warehouses are marked with an `isSystem` flag. These entities are protected from deletion to prevent accidental loss of fundamental platform resources.
- **Status:** PASSED

---

## Technical Execution
The tests use `mockStatic` to simulate the `SecurityUtil.getCurrentUserId()` call within the service logic. Execution command:
```bash
./gradlew test --tests com.nox.platform.module.warehouse.service.management.WarehouseServiceTest
```
