-- =========================================================================
-- Migration: V7__add_temp_mfa_secret.sql
-- Description: Adds a temporary storage column for MFA secrets during the enrollment handshake.
-- =========================================================================

ALTER TABLE user_security ADD COLUMN temp_mfa_secret TEXT;
