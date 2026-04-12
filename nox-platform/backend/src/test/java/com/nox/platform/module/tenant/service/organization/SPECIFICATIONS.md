# Test Specifications: Organization Management

This directory contains unit tests for the `OrganizationService`, which manages the high-level organization lifecycle, unique identity (slugs), and system-wide provisioning events.

## 1. Organization Lifecycle
Creating an organization is a complex process involving several related domain objects.

### ORG-01: Standard Organization Creation
When a valid user creates a new organization, the system must establish the core entity and automatically initialize three default roles: `OWNER` (level 100), `ADMIN` (level 50), and `MEMBER` (level 10). The creator is automatically added as the first `OWNER`.
- **Status:** PASSED

### ORG-02: Unique Slug Generation and Normalization
Organizations are identified by a URL-friendly "slug". The system normalizes the organization name (e.g., removing accents and spaces) to create this slug. If a collision occurs (slug already exists), the system must append a random 6-character hash to ensure uniqueness.
- **Status:** PASSED

### ORG-03: Downstream Provisioning Signal
Successful creation of an organization must trigger the `OrganizationCreatedEvent`. This ensures that other modules (like the Warehouse module) can automatically set up the necessary infrastructure for the new team.
- **Status:** PASSED

---

## 2. Secure Deletion
Organizations are never hard-deleted to preserve audit trails and prevent accidental data loss.

### ORG-04: Graceful Soft Delete
Deleting an organization triggers a cascading soft-delete. The organization, its members, and its custom roles are all marked as deleted. An `OrganizationDeletedEvent` is published to notify other modules to clean up their respective resources (e.g., stopping active compute blocks).
- **Status:** PASSED

---

## Technical Execution
The service is tested by mocking the `OrganizationRepository`, `RoleService`, and `OrgMemberRepository`. Execution command:
```bash
./gradlew test --tests com.nox.platform.module.tenant.service.organization.OrganizationServiceTest
```
