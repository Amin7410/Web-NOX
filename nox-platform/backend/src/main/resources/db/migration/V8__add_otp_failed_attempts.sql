-- =========================================================================
-- Migration: V8__add_otp_failed_attempts.sql
-- Description: Introduces brute-force protection to OTP verification by tracking failed attempts.
-- =========================================================================

ALTER TABLE otp_codes ADD COLUMN failed_attempts INTEGER NOT NULL DEFAULT 0;
