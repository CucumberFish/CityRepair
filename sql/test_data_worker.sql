-- ========================================
-- 维修人员模块测试数据
-- 用于测试维修人员工单处理功能
-- ========================================

-- 1. 更新工单1的状态为PENDING_ASSIGN，准备进行分派测试
UPDATE repair_order SET
    status = 'PENDING_ASSIGN',
    priority = 'HIGH',
    updated_at = NOW()
WHERE id = 1;

-- 插入状态日志：审核通过
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (1, 1, 'APPROVE', 'PENDING_REVIEW', 'PENDING_ASSIGN', '审核通过，影响通行，优先处理', NOW());

-- 2. 创建分派记录：将工单1分派给worker1
INSERT INTO order_assignment (order_id, admin_id, worker_id, assigned_at, is_current, remark)
VALUES (1, 1, 3, NOW(), 1, '分派给维修师傅一号处理');

-- 更新工单的当前维修人员和状态
UPDATE repair_order SET
    current_worker_id = 3,
    status = 'PENDING_ACCEPT',
    assigned_at = NOW(),
    updated_at = NOW()
WHERE id = 1;

-- 插入状态日志：分派
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (1, 1, 'ASSIGN', 'PENDING_ASSIGN', 'PENDING_ACCEPT', '管理员分派给维修师傅一号', NOW());

-- 3. 创建第二个工单用于测试（工单状态为PENDING_ACCEPT）
INSERT INTO repair_order (
    order_no, resident_id, category_id, title, location, description,
    contact_phone, priority, status, current_worker_id, assigned_at,
    version, created_at, updated_at
) VALUES (
    'RO202606250002',
    2,  -- resident1
    2,  -- 路灯照明
    '小区路灯损坏',
    '幸福小区东门',
    '东门入口处路灯不亮，夜间出行不便',
    '13800000002',
    'NORMAL',
    'PENDING_ACCEPT',
    3,  -- worker1
    NOW(),
    0,
    NOW(),
    NOW()
);

-- 为工单2创建分派记录
INSERT INTO order_assignment (order_id, admin_id, worker_id, assigned_at, is_current, remark)
VALUES (LAST_INSERT_ID(), 1, 3, NOW(), 1, '分派给维修师傅一号');

-- 插入状态日志：工单2创建
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 1, 2, 'CREATE', NULL, 'PENDING_REVIEW', '居民提交报修', NOW());

-- 插入状态日志：工单2审核通过
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 2, 1, 'APPROVE', 'PENDING_REVIEW', 'PENDING_ASSIGN', '审核通过', NOW());

-- 插入状态日志：工单2分派
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 3, 1, 'ASSIGN', 'PENDING_ASSIGN', 'PENDING_ACCEPT', '分派给维修师傅一号', NOW());

-- 4. 创建第三个工单用于测试处理中状态
INSERT INTO repair_order (
    order_no, resident_id, category_id, title, location, description,
    contact_phone, priority, status, current_worker_id, assigned_at, accepted_at,
    version, created_at, updated_at
) VALUES (
    'RO202606250003',
    2,  -- resident1
    3,  -- 供水排水
    '水管漏水',
    '幸福小区3号楼',
    '3号楼2单元水管接头处漏水，需要紧急处理',
    '13800000002',
    'URGENT',
    'PROCESSING',
    3,  -- worker1
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_SUB(NOW(), INTERVAL 12 HOUR),
    0,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    NOW()
);

-- 为工单3创建分派记录（已接单）
INSERT INTO order_assignment (order_id, admin_id, worker_id, assigned_at, accepted_at, is_current, remark)
VALUES (LAST_INSERT_ID(), 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), 1, '紧急处理水管漏水');

-- 插入状态日志：工单3完整流程
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 2, 2, 'CREATE', NULL, 'PENDING_REVIEW', '居民提交报修', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 3, 1, 'APPROVE', 'PENDING_REVIEW', 'PENDING_ASSIGN', '审核通过', DATE_SUB(NOW(), INTERVAL 23 HOUR));

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 4, 1, 'ASSIGN', 'PENDING_ASSIGN', 'PENDING_ACCEPT', '分派给维修师傅一号', DATE_SUB(NOW(), INTERVAL 22 HOUR));

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 5, 3, 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '维修人员接单', DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- 插入一条进度日志
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 6, 3, 'PROGRESS', 'PROCESSING', 'PROCESSING', '已到现场，确认需要更换水管接头', DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- 5. 创建第四个工单用于测试已完成状态
INSERT INTO repair_order (
    order_no, resident_id, category_id, title, location, description,
    contact_phone, priority, status, current_worker_id,
    assigned_at, accepted_at, completed_at, completion_result,
    version, created_at, updated_at
) VALUES (
    'RO202606250004',
    2,  -- resident1
    1,  -- 道路破损
    '人行道砖块松动',
    '幸福小区北门',
    '北门人行道有几块砖松动，存在安全隐患',
    '13800000002',
    'NORMAL',
    'COMPLETED',
    3,  -- worker1
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    '已完成人行道砖块修复，共更换5块砖，道路恢复平整',
    0,
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 为工单4创建分派记录（已完成）
INSERT INTO order_assignment (order_id, admin_id, worker_id, assigned_at, accepted_at, is_current, remark)
VALUES (LAST_INSERT_ID(), 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 1, '修复人行道砖块');

-- 插入状态日志：工单4完整流程
INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 2, 2, 'CREATE', NULL, 'PENDING_REVIEW', '居民提交报修', DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 3, 1, 'APPROVE', 'PENDING_REVIEW', 'PENDING_ASSIGN', '审核通过', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 2 HOUR);

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 4, 1, 'ASSIGN', 'PENDING_ASSIGN', 'PENDING_ACCEPT', '分派给维修师傅一号', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 3 HOUR);

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 5, 3, 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '维修人员接单', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark, created_at)
VALUES (LAST_INSERT_ID() - 6, 3, 'COMPLETE', 'PROCESSING', 'COMPLETED', '工单处理完成', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ========================================
-- 验证数据
-- ========================================

-- 查询所有工单状态
SELECT id, order_no, title, status, current_worker_id, priority
FROM repair_order
ORDER BY id;

-- 查询所有派单记录
SELECT id, order_id, worker_id, is_current, assigned_at, accepted_at
FROM order_assignment
ORDER BY id;

-- 查询所有状态日志
SELECT id, order_id, operator_id, action, from_status, to_status, remark, created_at
FROM order_status_log
ORDER BY order_id, created_at;
