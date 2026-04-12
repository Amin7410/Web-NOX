# Test Specifications: MFA Verification Service

This directory contains unit tests for the `MfaVerificationService`, which manages the second factor of the authentication process, including TOTP (Time-based One-Time Password) validation and emergency backup code handling.

## 1. Standard MFA Verification Flow
These tests ensure that users who have successfully provided their primary credentials can complete the authentication process using their MFA device.

### MFA-01: Successful Code Verification
When a user provides the correct 6-digit code from their authenticator app, the system must validate it against their stored MFA secret. Upon success, the system resets the failed attempt counters and generates the final session tokens (JWT and Refresh token).
- **Status:** PASSED

### MFA-02: Invalid Code Handling
If a user provides an incorrect or expired MFA code, the system must reject the request with an `INVALID_MFA_CODE` exception. Crucially, the system must also increment the `failedMfaAttempts` counter to track potential brute-force attacks on the second factor.
- **Status:** PASSED

### MFA-03: Security Lockout (Brute Force Protection)
To protect against automated attempts to guess MFA codes, the system enforces a strict lockout policy. After 5 consecutive failed MFA attempts, the account must be automatically locked for 15 minutes. This test verifies that the `lockAccount` logic is correctly triggered at the threshold.
- **Status:** PASSED

---

## 2. Emergency Backup Code Recovery
Backup codes are used when a user loses access to their primary MFA device. These codes are single-use and highly sensitive.

### MFA-04: Successful Backup Code Login
A user can provide one of their pre-generated backup codes to bypass the TOTP requirement. The system must verify the code's hash against the database, ensure it hasn't been used yet, and then mark it as used permanently after a successful login.
- **Status:** PASSED

### MFA-05: Invalid or Used Backup Code Prevention
The system must reject any attempt to use a backup code that is either incorrect or has already been marked as used. This ensures that backup codes remain a secure, one-time-only recovery mechanism.
- **Status:** PASSED

---

## 3. Token Integrity and Lifecycle
MFA verification depends on a valid "MFA Pending" token generated during the primary login phase.

### MFA-06: Verification Token Validation
The system must verify that the provided token contains the `mfa_pending` claim. If the token is invalid, expired, or doesn't belong to the "pending" category, the verification process must be aborted with an `INVALID_MFA_TOKEN` exception.
- **Status:** PASSED

---

## Technical Execution
The tests employ Mockito to isolate the service from the TOTP generation logic (`MfaService`) and the database. Execution command:
```bash
./gradlew test --tests com.nox.platform.module.iam.service.mfa.MfaVerificationServiceTest
```
