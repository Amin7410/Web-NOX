-- V18__add_pessimistic_lock_to_core_blocks.sql

ALTER TABLE core_blocks ADD COLUMN locked_by UUID NULL;
ALTER TABLE core_blocks ADD COLUMN locked_at TIMESTAMP WITH TIME ZONE NULL;

-- Cannot reliably use foreign key constraint on locked_by to users because IAM schema might be in a separate boundary or namespace in complex microservices. But since both are in same DB for now, it's optional, but we'll leave it simple.
