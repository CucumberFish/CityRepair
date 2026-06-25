-- ========================================
-- 重置测试数据：创建待接单工单
-- 请在MySQL客户端中执行此脚本
-- ========================================

-- 将工单4重置为待接单状态
UPDATE repair_order SET
    status = 'PENDING_ACCEPT',
    current_worker_id = 3,
    accepted_at = NULL,
    completed_at = NULL,
    completion_result = NULL,
    updated_at = NOW()
WHERE id = 4;

-- 删除工单4的相关状态日志（保留创建和分派日志）
DELETE FROM order_status_log
WHERE order_id = 4 AND action IN ('ACCEPT', 'START', 'PROGRESS', 'COMPLETE');

-- 将工单5也重置为待接单状态
UPDATE repair_order SET
    status = 'PENDING_ACCEPT',
    current_worker_id = 3,
    accepted_at = NULL,
    completed_at = NULL,
    completion_result = NULL,
    updated_at = NOW()
WHERE id = 5;

-- 删除工单5的相关状态日志
DELETE FROM order_status_log
WHERE order_id = 5 AND action IN ('ACCEPT', 'START', 'PROGRESS', 'COMPLETE');

-- 验证结果
SELECT id, order_no, title, status, current_worker_id
FROM repair_order
WHERE current_worker_id = 3
ORDER BY id;
