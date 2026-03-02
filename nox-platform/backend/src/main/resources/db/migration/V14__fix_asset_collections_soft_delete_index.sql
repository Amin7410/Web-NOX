-- V14__fix_asset_collections_soft_delete_index.sql
-- Drop the original unique index which did not account for soft-deleted items
DROP INDEX IF EXISTS idx_asset_collections_warehouse_name;

-- Recreate the unique index, but only enforce uniqueness for active (non-deleted) items
CREATE UNIQUE INDEX idx_asset_collections_warehouse_name ON asset_collections (warehouse_id, name) WHERE deleted_at IS NULL;
