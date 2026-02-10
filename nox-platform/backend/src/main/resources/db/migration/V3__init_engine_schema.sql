-- 2. MULTI-TENANCY (Essential Tables)

-- Table: organizations
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    settings JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_organizations_slug ON organizations (slug) WHERE deleted_at IS NULL;

-- Table: roles
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    permissions TEXT[] NOT NULL DEFAULT '{}'
);

CREATE UNIQUE INDEX idx_roles_org_name ON roles (org_id, name);
CREATE INDEX idx_roles_org_id ON roles (org_id);

-- Table: org_members
CREATE TABLE org_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    invited_by UUID REFERENCES users(id)
);

CREATE UNIQUE INDEX idx_org_members_org_user ON org_members (org_id, user_id);
CREATE INDEX idx_org_members_user_id ON org_members (user_id);
CREATE INDEX idx_org_members_role_id ON org_members (role_id);

-- 4. CORE ENGINE (RUNTIME)

-- Table: projects
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    visibility VARCHAR(50) NOT NULL DEFAULT 'PRIVATE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_projects_org_slug ON projects (org_id, slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_projects_org_created ON projects (org_id, created_at);
CREATE INDEX idx_projects_created_by ON projects (created_by_id);

-- Table: workspaces
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_workspaces_project_created ON workspaces (project_id, created_at);
CREATE INDEX idx_workspaces_created_by ON workspaces (created_by);

-- Table: core_blocks
CREATE TABLE core_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    parent_block_id UUID REFERENCES core_blocks(id) ON DELETE CASCADE,
    origin_asset_id UUID REFERENCES assets_block_templates(id) ON DELETE SET NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    config JSONB NOT NULL DEFAULT '{}',
    visual JSONB NOT NULL DEFAULT '{}',
    created_by_id UUID REFERENCES users(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_core_blocks_filter ON core_blocks (workspace_id, type) WHERE deleted_at IS NULL;
CREATE INDEX idx_core_blocks_parent ON core_blocks (parent_block_id);
CREATE INDEX idx_core_blocks_origin ON core_blocks (origin_asset_id);
CREATE INDEX idx_core_blocks_created_by ON core_blocks (created_by_id);

-- Table: block_invader_usages
CREATE TABLE block_invader_usages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    block_id UUID NOT NULL REFERENCES core_blocks(id) ON DELETE CASCADE,
    invader_asset_id UUID NOT NULL REFERENCES assets_invader_definitions(id) ON DELETE CASCADE,
    applied_version VARCHAR(20),
    config_snapshot JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_block_invader_usages_block_invader ON block_invader_usages (block_id, invader_asset_id);
CREATE INDEX idx_block_invader_usages_invader ON block_invader_usages (invader_asset_id);

-- Table: core_relations
CREATE TABLE core_relations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    source_block_id UUID NOT NULL REFERENCES core_blocks(id) ON DELETE CASCADE,
    target_block_id UUID NOT NULL REFERENCES core_blocks(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    rules JSONB NOT NULL DEFAULT '{}',
    visual JSONB NOT NULL DEFAULT '{}',
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_core_relations_workspace ON core_relations (workspace_id);
CREATE UNIQUE INDEX idx_core_relations_source_target ON core_relations (source_block_id, target_block_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_core_relations_target ON core_relations (target_block_id);

-- Table: core_snapshots
CREATE TABLE core_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    commit_message TEXT,
    full_state_dump JSONB NOT NULL,
    created_by_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_core_snapshots_project_created ON core_snapshots (project_id, created_at);
CREATE INDEX idx_core_snapshots_created_by ON core_snapshots (created_by_id);
