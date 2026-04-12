-- =========================================================================
-- Migration: V17__add_soft_delete_and_status_to_workspaces.sql
-- Description: Introduces lifecycle status tracking to workplace entities.
-- =========================================================================

ALTER TABLE workspaces ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT' NOT NULL;
