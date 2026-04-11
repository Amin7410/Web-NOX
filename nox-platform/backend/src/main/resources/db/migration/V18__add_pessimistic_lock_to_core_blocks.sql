-- =========================================================================
-- Migration: V18__add_pessimistic_lock_to_core_blocks.sql
-- Description: Introduces Mutex-based pessimistic locking for real-time canvas collaboration.
-- =========================================================================

ALTER TABLE core_blocks ADD COLUMN locked_by UUID NULL;
ALTER TABLE core_blocks ADD COLUMN locked_at TIMESTAMP WITH TIME ZONE NULL;
