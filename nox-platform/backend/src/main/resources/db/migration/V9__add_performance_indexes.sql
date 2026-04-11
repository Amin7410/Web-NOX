-- =========================================================================
-- Migration: V9__add_performance_indexes.sql
-- Description: Applies strategic indexes to optimize frequent lookups and background data pruning.
-- =========================================================================

CREATE INDEX idx_otp_codes_lookup ON otp_codes(user_id, type, used_at);

CREATE INDEX idx_user_sessions_cleanup ON user_sessions(expires_at, revoked_at);

CREATE INDEX idx_audit_logs_org_created ON audit_logs(org_id, created_at DESC);
