-- Phá bỏ giới hạn 1 cặp Block - 1 Dây nối
-- Cho phép nối nhiều dây giữa cùng 2 Block nếu chúng nối vào các Cổng (Handle) khác nhau.

-- 1. Xoá bỏ Index cũ (Quá khắt khe)
DROP INDEX IF EXISTS idx_core_relations_source_target;

-- 2. Tạo Index mới thông minh hơn (Phân biệt theo Cổng cắm)
-- Dùng cú pháp (visual ->> 'sourceHandle') để lấy giá trị text từ JSONB
CREATE UNIQUE INDEX idx_core_relations_multi_wire 
ON core_relations (
    source_block_id, 
    (visual ->> 'sourceHandle'), 
    target_block_id, 
    (visual ->> 'targetHandle')
) 
WHERE deleted_at IS NULL;

-- 3. Thông báo cho Hibernate qua ddl-auto nếu cần, nhưng flyway sẽ xử lý migration này.
