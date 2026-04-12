-- =========================================================================
-- Migration: V13__add_level_to_roles.sql
-- Description: Introduces hierarchical role levels for granular permission precedence.
-- =========================================================================

ALTER TABLE roles ADD COLUMN IF NOT EXISTS level INTEGER NOT NULL DEFAULT 0;
