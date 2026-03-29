-- V15: Add version for Optimistic Locking to prevent data loss in Core Blocks
ALTER TABLE core_blocks ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
