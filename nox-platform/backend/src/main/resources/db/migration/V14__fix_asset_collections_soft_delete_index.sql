-- =========================================================================
-- Migration: V14__fix_asset_collections_soft_delete_index.sql
-- Description: Refines unique constraints to account for logically deleted asset collections.
-- =========================================================================
DROP INDEX IF EXISTS idx_asset_collections_warehouse_name;

CREATE UNIQUE INDEX idx_asset_collections_warehouse_name ON asset_collections (warehouse_id, name) WHERE deleted_at IS NULL;
