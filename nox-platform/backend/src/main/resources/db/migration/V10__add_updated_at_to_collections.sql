-- V10: Fix missing updated_at in asset_collections 

ALTER TABLE asset_collections
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
