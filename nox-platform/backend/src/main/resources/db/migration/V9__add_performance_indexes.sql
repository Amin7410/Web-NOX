-- V9: Performance Indexes for OTP and Sessions

-- 1. Optimize OTP lookup and invalidation
-- Existing: idx_otp_codes_user_id (user_id)
-- New composite index to speed up finding active OTPs and invalidating previous ones
CREATE INDEX idx_otp_codes_lookup ON otp_codes(user_id, type, used_at);

-- 2. Optimize Session Cleanup and expiration checks
-- New index to speed up background cleanup tasks
CREATE INDEX idx_user_sessions_cleanup ON user_sessions(expires_at, revoked_at);

-- 3. Optimize Audit Log target lookups
-- Already has idx_audit_logs_target ON audit_logs(target_type, target_id) from V4
-- but adding time-sorted index for faster audit history feeds
CREATE INDEX idx_audit_logs_org_created ON audit_logs(org_id, created_at DESC);
