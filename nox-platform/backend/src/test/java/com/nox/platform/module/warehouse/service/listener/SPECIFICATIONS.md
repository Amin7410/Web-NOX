# Test Specifications: Warehouse Event Listeners

This directory contains unit tests for the `WarehouseEventListener`, which automates the provisioning and cleanup of warehouses based on system-wide events.

## 1. Automated Provisioning
These tests ensure that users and organizations have their storage space ready as soon as they are created.

### LST-01: User Personal Warehouse Auto-Creation
When a `UserCreatedEvent` is received (signaling a new active user), the listener must automatically trigger the creation of a personal warehouse. This test verifies that the `internalCreateWarehouse` method is called with the correct user ID and owner type.
- **Status:** PASSED

### LST-02: Organization Warehouse Auto-Creation
Similarly, the creation of a new organization (`OrganizationCreatedEvent`) must result in the automatic setup of an organization-level warehouse. This allows teams to start collaborating immediately.
- **Status:** PASSED

---

## 2. Robustness and Idempotency
To prevent errors in case of event retries, the listener must handle duplicate events gracefully.

### LST-03: Event Idempotency Check
If an event is received for an owner who already possesses a warehouse (e.g., due to a message retry), the listener must verify the existing state and skip the creation process. This prevents `WAREHOUSE_EXISTS` exceptions and ensures system stability.
- **Status:** PASSED

---

## Technical Execution
The tests verify interactions between the event listener and the `WarehouseService` using Mockito's `verify`. Execution command:
```bash
./gradlew test --tests com.nox.platform.module.warehouse.service.listener.WarehouseEventListenerTest
```
