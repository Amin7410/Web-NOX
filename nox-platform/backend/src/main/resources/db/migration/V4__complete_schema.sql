-- ==========================================
-- SECTION 2 (REMAINDER): MULTI-TENANCY & BILLING
-- ==========================================

-- Subscriptions
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL UNIQUE,
    plan_id VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    stripe_customer_id VARCHAR(255),
    stripe_sub_id VARCHAR(255),
    quotas JSONB NOT NULL DEFAULT '{}',
    current_period_start TIMESTAMPTZ,
    current_period_end TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_subscriptions_org FOREIGN KEY (org_id) REFERENCES organizations(id)
);

CREATE INDEX idx_subscriptions_stripe_customer ON subscriptions(stripe_customer_id);

-- Org Usage Metrics
CREATE TABLE org_usage_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    current_value BIGINT DEFAULT 0,
    reset_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ DEFAULT NOW(),

    CONSTRAINT fk_org_usage_metrics_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT uq_org_usage_metrics_type UNIQUE (org_id, metric_type)
);

CREATE INDEX idx_org_usage_metrics_org ON org_usage_metrics(org_id);

-- API Keys
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    key_prefix VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    org_id UUID NOT NULL,
    scopes TEXT[],
    last_used_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_api_keys_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_keys_org FOREIGN KEY (org_id) REFERENCES organizations(id)
);

CREATE INDEX idx_api_keys_user ON api_keys(user_id);
CREATE INDEX idx_api_keys_org ON api_keys(org_id);


-- ==========================================
-- SECTION 5: COLLABORATION & OPS
-- ==========================================

-- Comments
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_target ON comments(target_type, target_id, created_at);
CREATE INDEX idx_comments_author ON comments(author_id);
CREATE INDEX idx_comments_parent ON comments(parent_comment_id);

-- Notes
CREATE TABLE notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type VARCHAR(50) NOT NULL, -- e.g., 'PROJECT', 'USER'
    owner_id UUID NOT NULL,
    title VARCHAR(255),
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_notes_owner ON notes(owner_type, owner_id);

-- Chat Rooms
CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    project_id UUID,
    name VARCHAR(255) NOT NULL,

    CONSTRAINT fk_chat_rooms_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_chat_rooms_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_chat_rooms_org ON chat_rooms(org_id);
CREATE INDEX idx_chat_rooms_project ON chat_rooms(project_id);

-- Chat Messages
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    sender_id UUID, -- Nullable if system message or user deleted? Design said Set Null
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_chat_messages_room_time ON chat_messages(room_id, created_at);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);

-- Files
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploader_id UUID NOT NULL,
    project_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_files_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_files_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE INDEX idx_files_project_time ON files(project_id, created_at);
CREATE INDEX idx_files_uploader ON files(uploader_id);

-- File Attachments
CREATE TABLE file_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,

    CONSTRAINT fk_file_attachments_file FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_file_attachments_file_target ON file_attachments(file_id, target_type, target_id);
CREATE INDEX idx_file_attachments_target ON file_attachments(target_type, target_id);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id UUID,
    metadata JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_logs_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_logs_org_time ON audit_logs(org_id, created_at);
CREATE INDEX idx_audit_logs_target ON audit_logs(target_type, target_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);

-- Edit Locks
CREATE TABLE edit_locks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID NOT NULL,
    locked_by UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_edit_locks_user FOREIGN KEY (locked_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_edit_locks_resource UNIQUE (resource_type, resource_id)
);

CREATE INDEX idx_edit_locks_user ON edit_locks(locked_by);

-- Feature Flags
CREATE TABLE feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    default_value JSONB NOT NULL
);

-- Org Feature Overrides
CREATE TABLE org_feature_overrides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    feature_flag_id UUID NOT NULL,
    value JSONB NOT NULL,

    CONSTRAINT fk_org_feature_overrides_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_org_feature_overrides_flag FOREIGN KEY (feature_flag_id) REFERENCES feature_flags(id) ON DELETE CASCADE,
    CONSTRAINT uq_org_feature_overrides UNIQUE (org_id, feature_flag_id)
);

CREATE INDEX idx_org_feature_overrides_flag ON org_feature_overrides(feature_flag_id);
