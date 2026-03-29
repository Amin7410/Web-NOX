-- V16__add_deleted_at_to_block_invader_usages.sql
-- Add soft-delete ability to Invaders mapping to safely recycle data.

ALTER TABLE block_invader_usages ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;

-- Remove the hardware CASCADE since we rely on logical soft deletes now
-- First, drop the existing constraint (PostgreSQL auto-generated name)
DO $$
BEGIN
    -- Try to drop the constraint if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_core_blocks_invader_usages' 
        AND table_name = 'block_invader_usages'
    ) THEN
        ALTER TABLE block_invader_usages DROP CONSTRAINT fk_core_blocks_invader_usages;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name LIKE '%block_invader_usages_block_id_fkey' 
        AND table_name = 'block_invader_usages'
    ) THEN
        ALTER TABLE block_invader_usages DROP CONSTRAINT block_invader_usages_block_id_fkey;
    END IF;
END $$;

-- Re-add it without CASCADE to prevent accidental hard deletes when dealing with legacy raw DB operations 
ALTER TABLE block_invader_usages
ADD CONSTRAINT fk_core_blocks_invader_usages 
FOREIGN KEY (block_id) 
REFERENCES core_blocks(id);
