-- =========================================================================
-- Migration: V20__add_workspace_status.sql
-- Description: Ensures presence of workspace status column across all environments.
-- =========================================================================

ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
