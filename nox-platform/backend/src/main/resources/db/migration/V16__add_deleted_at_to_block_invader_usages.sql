-- =========================================================================
-- Migration: V16__add_deleted_at_to_block_invader_usages.sql
-- Description: Integrates Soft-Delete support into Invader usage mapping entities.
-- =========================================================================

ALTER TABLE block_invader_usages ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;

DO $$
BEGIN
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

ALTER TABLE block_invader_usages
ADD CONSTRAINT fk_core_blocks_invader_usages 
FOREIGN KEY (block_id) 
REFERENCES core_blocks(id);
