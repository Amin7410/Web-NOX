-- Flyway Migration: Backfill warehouses for existing users
-- Goal: Ensure every active user has at least one personal warehouse

INSERT INTO warehouses (id, owner_id, owner_type, name, is_system, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    'USER',
    'Personal Warehouse',
    false,
    NOW(),
    NOW()
FROM users u
WHERE u.deleted_at IS NULL
  AND u.status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM warehouses w 
    WHERE w.owner_id = u.id 
      AND w.owner_type = 'USER' 
      AND w.deleted_at IS NULL
  );
