-- =========================================================================
-- Migration: V11__scoped_invader_code_unique.sql
-- Description: Transitions from global to warehouse-scoped uniqueness for Invader definition codes.
-- =========================================================================

ALTER TABLE assets_invader_definitions DROP CONSTRAINT IF EXISTS assets_invader_definitions_code_key;

CREATE UNIQUE INDEX uq_invader_warehouse_code 
ON assets_invader_definitions (warehouse_id, code) 
WHERE deleted_at IS NULL;
