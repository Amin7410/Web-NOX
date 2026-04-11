-- =========================================================================
-- Migration: V24__partition_and_indexes.sql
-- Description: Applies indexing optimizations and reconstructs massive tables for partitioning.
-- =========================================================================

-- PART 1: Remove redundant indexes (Redundant via Left-Prefix Rule on Unique Constraints)
DROP INDEX IF EXISTS idx_roles_org_id;
DROP INDEX IF EXISTS idx_org_usage_metrics_org;

-- PART 2: Performance indexing for time-based background cleanup jobs
CREATE INDEX IF NOT EXISTS idx_otp_codes_expires_at ON otp_codes(expires_at);
CREATE INDEX IF NOT EXISTS idx_invitations_expires_at ON invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_api_keys_expires_at ON api_keys(expires_at);
CREATE INDEX IF NOT EXISTS idx_edit_locks_expires_at ON edit_locks(expires_at);

-- PART 3: Enable pg_trgm extension and apply GIN indexes for fuzzy Full-Text Search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_users_full_name_trgm ON users USING GIN (full_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_projects_name_trgm ON projects USING GIN (name gin_trgm_ops);


-- PART 4: Architectural Partitioning for high-throughput tables
-- Note: Rebuilding tables dynamically. Compatible with pre-production initialization.
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS core_snapshots CASCADE;

-- Table: audit_logs (Partitioned by RANGE on created_at)
-- Purpose: Enables aggressive horizontal scaling and efficient partition-level pruning for old events.
CREATE TABLE audit_logs (
    id UUID DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id UUID,
    metadata JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    PRIMARY KEY (id, created_at),
    CONSTRAINT fk_audit_logs_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
) PARTITION BY RANGE (created_at);

CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;

CREATE INDEX idx_audit_logs_org_time ON audit_logs(org_id, created_at);
CREATE INDEX idx_audit_logs_target ON audit_logs(target_type, target_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);

-- Table: chat_messages (Partitioned by RANGE on created_at)
-- Purpose: Optimizes real-time retrieval performance and archiving processes for time-series chat data.
CREATE TABLE chat_messages (
    id UUID DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    sender_id UUID,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id, created_at),
    CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
) PARTITION BY RANGE (created_at);

CREATE TABLE chat_messages_default PARTITION OF chat_messages DEFAULT;

CREATE INDEX idx_chat_messages_room_time ON chat_messages(room_id, created_at);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);

-- Table: core_snapshots (Partitioned by HASH on project_id)
-- Purpose: Distributes heavy JSONB payload storage workloads uniformly across discrete partitions.
CREATE TABLE core_snapshots (
    id UUID DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    commit_message TEXT,
    full_state_dump JSONB NOT NULL,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    PRIMARY KEY (id, project_id),
    CONSTRAINT fk_core_snapshots_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_core_snapshots_creator FOREIGN KEY (created_by_id) REFERENCES users(id)
) PARTITION BY HASH (project_id);

CREATE TABLE core_snapshots_p0 PARTITION OF core_snapshots FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE core_snapshots_p1 PARTITION OF core_snapshots FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE core_snapshots_p2 PARTITION OF core_snapshots FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE core_snapshots_p3 PARTITION OF core_snapshots FOR VALUES WITH (MODULUS 4, REMAINDER 3);

CREATE INDEX idx_core_snapshots_project_created ON core_snapshots (project_id, created_at);
CREATE INDEX idx_core_snapshots_created_by ON core_snapshots (created_by_id);
