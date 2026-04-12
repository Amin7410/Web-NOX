-- =========================================================================
-- Migration: V15__add_version_to_core_blocks.sql
-- Description: Enables Optimistic Concurrency Control (OCC) using a version field for Core Blocks.
-- =========================================================================
ALTER TABLE core_blocks ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
