# Test Specifications: Authentication Service

This directory contains unit tests for the `AuthenticationService`, which manages user login sessions, security lockout logic, and multi-factor authentication (MFA) requirements.

## 1. Primary Authentication Scenarios
These tests ensure that valid users can access their accounts while unauthorized access is blocked based on account status.

### AUTH-01: Standard Login Success
When a valid user (ACTIVE status, correct password, MFA disabled) attempts to login, the system must generate a successful authentication result. This includes a valid JWT token and a refresh token. It also ensures that any previous failed login attempts are reset.
- **Status:** PASSED

### AUTH-02: Multi-Factor Authentication (MFA) Requirement
If a user has MFA enabled, the standard login flow must not return the final access tokens immediately. Instead, the system must return an `mfa_pending` status and a temporary MFA token. This forces the client to proceed to the second verification step.
- **Status:** PASSED

### AUTH-03: Locked Account Protection
The system must identify if an account is currently under a temporary lockout (due to previous failures). Any attempt to authenticate during this period must be rejected with an `ACCOUNT_LOCKED` exception, regardless of whether the password provided is correct.
- **Status:** PASSED

### AUTH-04: Inactive Account Rejection
Users who have not yet verified their email (`PENDING_VERIFICATION`) must be denied access to the session generation logic. The system throws an `ACCOUNT_NOT_ACTIVE` exception, prompting them to complete the registration flow.
- **Status:** PASSED

---

## 2. Security and Brute-Force Protection
These tests validate the integrity of the "Exponential Backoff" and "Account Lockout" mechanisms.

### AUTH-05: Failed Attempt Tracking
Upon a failed login attempt (invalid password), the system must increment the user's `failedLoginAttempts` counter. This ensures that the system is aware of ongoing brute-force attempts targeting a specific account.
- **Status:** PASSED

### AUTH-06: Automatic Account Lockout
When the number of consecutive failed attempts reaches the threshold (set at 5), the system must automatically lock the account for a predefined duration (15 minutes). This test verifies that the `internalSecurityStateService` is correctly invoked to apply the lock.
- **Status:** PASSED

---

## Technical Execution
The service is tested by mocking the `AuthenticationManager` (Spring Security), `UserRepository`, and `TokenProvider`. Execution command:
```bash
./gradlew test --tests com.nox.platform.module.iam.service.authentication.AuthenticationServiceTest
```
