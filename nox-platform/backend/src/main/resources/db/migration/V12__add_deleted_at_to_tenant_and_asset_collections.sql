-- =========================================================================
-- Migration: V12__add_deleted_at_to_tenant_and_asset_collections.sql
-- Description: Expands Soft-Delete support to Organization Roles, Members, and Asset Collections.
-- =========================================================================
ALTER TABLE roles ADD COLUMN deleted_at TIMESTAMPTZ;
ALTER TABLE org_members ADD COLUMN deleted_at TIMESTAMPTZ;
ALTER TABLE asset_collections ADD COLUMN deleted_at TIMESTAMPTZ;

DROP INDEX idx_roles_org_name;
CREATE UNIQUE INDEX idx_roles_org_name ON roles (org_id, name) WHERE deleted_at IS NULL;

DROP INDEX idx_org_members_org_user;
CREATE UNIQUE INDEX idx_org_members_org_user ON org_members (org_id, user_id) WHERE deleted_at IS NULL;
