-- =========================================================================
-- Migration: V19__add_created_at_to_core_relations.sql
-- Description: Adds creation timestamps and supporting performance indexes to Blocks and Relations.
-- =========================================================================

ALTER TABLE core_relations ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE core_blocks ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

CREATE INDEX idx_core_relations_created_at ON core_relations (created_at);
CREATE INDEX idx_core_blocks_created_at ON core_blocks (created_at);
