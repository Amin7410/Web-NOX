# Test Specifications: User Registration Service

This directory contains unit tests for the `UserRegistrationService`, which handles user onboarding, email verification, and duplicate prevention logic. These tests use Mockito to isolate the service from database and external provider dependencies.

## 1. User Registration Flow
Registration is the first point of entry for new users. The logic must handle various states of existing accounts.

### REG-01: Standard New User Registration
When a unique email and valid data are provided, the system must create a new user entity with a `PENDING_VERIFICATION` status. It must also ensure the password is encrypted, an OTP is generated, and a `UserRegisteredEvent` is published to trigger the notification system.
- **Status:** PASSED

### REG-02: Pending Account Override
If a user tries to register with an email that already exists but is still `PENDING_VERIFICATION`, the system should allow them to "re-register". This updates their profile information (e.g., Full Name) and generates a fresh OTP. This provides a seamless experience for users who lost their initial verification email.
- **Status:** PASSED

### REG-03: Active Account Prevention
The system must prohibit registration with an email that is already verified and active. Any such attempt must be rejected with an `EMAIL_ALREADY_EXISTS` domain exception to prevent account hostile takeover or data corruption.
- **Status:** PASSED

### REG-04: Deleted Account Protection (Anti-Zombie)
To maintain system integrity and comply with security best practices, emails associated with deleted accounts cannot be recycled for new registrations. The system recognizes these "zombie" accounts and blocks registration explicitly.
- **Status:** PASSED

---

## 2. Email Verification Logic
Verification transitions a user from a pending state to an active state, enabling full access to the platform.

### VER-01: Successful Verification
When a user provides the correct OTP for their pending account, the system must transition their status to `ACTIVE`. It should also publish a `UserCreatedEvent`, which triggers downstream infrastructure processes like workspace provisioning.
- **Status:** PASSED

### VER-02: Redundant Verification Prevention
If an account is already at `ACTIVE` status, further verification attempts are unnecessary and could indicate a logic error in the client or a malicious attempt. The system rejects these attempts with a `USER_ALREADY_ACTIVE` error.
- **Status:** PASSED

---

## Technical Execution
The tests employ `MockitoExtension` to mock the repository, password encoder, and event publisher. Execution command:
```bash
./gradlew test --tests com.nox.platform.module.iam.service.registration.UserRegistrationServiceTest
```
