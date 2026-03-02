-- V11: Fix Global Unique Constraint on Invader Definition Code
-- Drop the existing global unique constraint from the code column.
-- Since the exact constraint name can vary or be auto-generated, we drop it.
-- In PostgreSQL, UNIQUE constraints create implicit indexes. 
-- Assuming it might be named `assets_invader_definitions_code_key`.

ALTER TABLE assets_invader_definitions DROP CONSTRAINT IF EXISTS assets_invader_definitions_code_key;

-- Add a scoped unique index instead
CREATE UNIQUE INDEX uq_invader_warehouse_code 
ON assets_invader_definitions (warehouse_id, code) 
WHERE deleted_at IS NULL;
