-- =========================================================================
-- Migration: V21_5__add_updated_at_to_warehouses.sql
-- Description: Standardizes warehouse entities with update tracking timestamps.
-- =========================================================================

ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
