# Test Specifications: Member Management

This directory contains unit tests for the `OrgMemberService`, which manages the relationships between users and organizations, including role assignments and security constraints.

## 1. Role Hierarchy and Onboarding
The system enforces a strict hierarchy to ensure that members cannot grant permissions they do not possess.

### MBR-01: High-Level Role Assignment
A member (the inviter) can add a new user to the organization if the target role has a level lower than or equal to the inviter's own role. For example, an `ADMIN` (level 50) is authorized to add a new `MEMBER` (level 10).
- **Status:** PASSED

### MBR-02: Privilege Escalation Prevention
The system must prohibit a user from assigning a role that has a higher level than their own. Any attempt by a `MODERATOR` (level 30) to appoint an `ADMIN` (level 50) is rejected with an `INSUFFICIENT_PRIVILEGE` exception.
- **Status:** PASSED

---

## 2. Organization Integrity (Safety Rails)
These tests ensure that the organization remains manageable and secure.

### MBR-03: Last Owner Protection
An organization must always have at least one `OWNER` to maintain accountability and administrative control. The system blocks any attempt to remove the final owner, throwing a `CANNOT_REMOVE_LAST_OWNER` error.
- **Status:** PASSED

### MBR-04: Standard Member Removal
A member (including an owner, if not the last one) can be removed from the organization. This removal is handled as a soft-delete to preserve membership history and related participation data.
- **Status:** PASSED

---

## Technical Execution
The service tests depend on the `OrgMemberRepository` and the `RoleService` (for role lookup). Execution command:
```bash
./gradlew test --tests com.nox.platform.module.tenant.service.member.OrgMemberServiceTest
```
