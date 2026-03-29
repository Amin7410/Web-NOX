-- Add created_at column to core_relations table
ALTER TABLE core_relations ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Add created_at column to core_blocks table (was missing from original schema)
ALTER TABLE core_blocks ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Add indexes for better query performance on the new ordering fields
CREATE INDEX idx_core_relations_created_at ON core_relations (created_at);
CREATE INDEX idx_core_blocks_created_at ON core_blocks (created_at);
