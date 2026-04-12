-- =========================================================================
-- Migration: V25__standardize_entity_audit_columns.sql
-- Description: Standardizes all core entities with created_at, updated_at and version columns
--              to support BaseEntity architecture and Optimistic Locking.
-- =========================================================================

-- 1. Standardizing Workspaces
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 2. Standardizing Roles
ALTER TABLE roles ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE roles ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 3. Standardizing Org Members
ALTER TABLE org_members ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE org_members ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0; 
ALTER TABLE org_members ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- 4. Standardizing Asset Collections
ALTER TABLE asset_collections ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE asset_collections ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE asset_collections ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- 5. Standardizing Projects & Organizations (Already have audit columns)
ALTER TABLE projects ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 6. Standardizing Core Relations
ALTER TABLE core_relations ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE core_relations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE core_relations ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 6. Standardizing Warehouses (Add version)
ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 7. Standardizing Assets (Block Templates & Invader Definitions)
ALTER TABLE assets_block_templates ADD COLUMN IF NOT EXISTS version_lock BIGINT NOT NULL DEFAULT 0; 
ALTER TABLE assets_invader_definitions ADD COLUMN IF NOT EXISTS version_lock BIGINT NOT NULL DEFAULT 0;

-- Update existing records to ensure version starts at 0
UPDATE workspaces SET version = 0 WHERE version IS NULL;
UPDATE roles SET version = 0 WHERE version IS NULL;
UPDATE org_members SET version = 0 WHERE version IS NULL;
UPDATE asset_collections SET version = 0 WHERE version IS NULL;
UPDATE projects SET version = 0 WHERE version IS NULL;
UPDATE organizations SET version = 0 WHERE version IS NULL;
UPDATE core_relations SET version = 0 WHERE version IS NULL;
UPDATE warehouses SET version = 0 WHERE version IS NULL;
UPDATE assets_block_templates SET version_lock = 0 WHERE version_lock IS NULL;
UPDATE assets_invader_definitions SET version_lock = 0 WHERE version_lock IS NULL;
