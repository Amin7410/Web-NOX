-- Flyway Migration: Add updated_at to warehouses table for consistency and to support backfill scripts
-- Added before V22 to ensure columns exist when V22/V23 run

ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
