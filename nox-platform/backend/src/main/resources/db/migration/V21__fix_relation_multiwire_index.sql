-- =========================================================================
-- Migration: V21__fix_relation_multiwire_index.sql
-- Description: Implements handle-aware unique constraints for core relations.
-- =========================================================================

DROP INDEX IF EXISTS idx_core_relations_source_target;

CREATE UNIQUE INDEX idx_core_relations_multi_wire 
ON core_relations (
    source_block_id, 
    (visual ->> 'sourceHandle'), 
    target_block_id, 
    (visual ->> 'targetHandle')
) 
WHERE deleted_at IS NULL;

-- 3. Thông báo cho Hibernate qua ddl-auto nếu cần, nhưng flyway sẽ xử lý migration này.
