-- =========================================================================
-- Migration: V22__backfill_org_warehouses.sql
-- Description: DML Translation: Backfills default warehouses for all existing organizations.
-- =========================================================================

INSERT INTO warehouses (id, owner_id, owner_type, name, is_system, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    o.id,
    'ORG',
    'Default Warehouse',
    false,
    NOW(),
    NOW()
FROM organizations o
WHERE o.deleted_at IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM warehouses w 
    WHERE w.owner_id = o.id 
      AND w.owner_type = 'ORG' 
      AND w.deleted_at IS NULL
  );
