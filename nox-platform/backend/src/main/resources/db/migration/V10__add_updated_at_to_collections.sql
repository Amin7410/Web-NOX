-- =========================================================================
-- Migration: V10__add_updated_at_to_collections.sql
-- Description: Standardizes audit tracking by adding missing updated_at timestamps to asset collections.
-- =========================================================================
ALTER TABLE asset_collections
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
