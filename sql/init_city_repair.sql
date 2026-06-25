-- City Repair and Work Order Processing System
-- MySQL 8 baseline schema and demo seed data.
-- Demo passwords are stored as {noop}123456 for prototype use. Replace them if the backend uses another password encoder.

CREATE DATABASE IF NOT EXISTS city_repair
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE city_repair;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS user_notification;
DROP TABLE IF EXISTS order_evaluation;
DROP TABLE IF EXISTS order_attachment;
DROP TABLE IF EXISTS order_status_log;
DROP TABLE IF EXISTS order_assignment;
DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS repair_category;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(32) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE repair_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_code VARCHAR(40) NOT NULL UNIQUE,
  category_name VARCHAR(80) NOT NULL,
  description VARCHAR(255),
  enabled TINYINT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_category_created_by FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE repair_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL UNIQUE,
  resident_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  location VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  contact_phone VARCHAR(20) NOT NULL,
  priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW',
  current_worker_id BIGINT,
  reject_reason VARCHAR(500),
  cancel_reason VARCHAR(500),
  completion_result TEXT,
  assigned_at DATETIME,
  accepted_at DATETIME,
  completed_at DATETIME,
  evaluated_at DATETIME,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_resident FOREIGN KEY (resident_id) REFERENCES sys_user(id),
  CONSTRAINT fk_order_category FOREIGN KEY (category_id) REFERENCES repair_category(id),
  CONSTRAINT fk_order_worker FOREIGN KEY (current_worker_id) REFERENCES sys_user(id),
  CONSTRAINT ck_order_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
  CONSTRAINT ck_order_status CHECK (status IN ('PENDING_REVIEW', 'PENDING_ASSIGN', 'PENDING_ACCEPT', 'PROCESSING', 'COMPLETED', 'EVALUATED', 'REJECTED', 'CANCELLED')),
  INDEX idx_order_resident_status (resident_id, status),
  INDEX idx_order_worker_status (current_worker_id, status),
  INDEX idx_order_category_status (category_id, status),
  INDEX idx_order_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  admin_id BIGINT NOT NULL,
  worker_id BIGINT NOT NULL,
  assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accepted_at DATETIME,
  canceled_at DATETIME,
  is_current TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(500),
  current_order_id BIGINT GENERATED ALWAYS AS (CASE WHEN is_current = 1 THEN order_id ELSE NULL END) STORED,
  CONSTRAINT fk_assignment_order FOREIGN KEY (order_id) REFERENCES repair_order(id),
  CONSTRAINT fk_assignment_admin FOREIGN KEY (admin_id) REFERENCES sys_user(id),
  CONSTRAINT fk_assignment_worker FOREIGN KEY (worker_id) REFERENCES sys_user(id),
  UNIQUE KEY uk_assignment_current_order (current_order_id),
  INDEX idx_assignment_worker_current (worker_id, is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_status_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  operator_id BIGINT NOT NULL,
  action VARCHAR(40) NOT NULL,
  from_status VARCHAR(32),
  to_status VARCHAR(32) NOT NULL,
  remark VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_order FOREIGN KEY (order_id) REFERENCES repair_order(id),
  CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id),
  CONSTRAINT ck_log_from_status CHECK (from_status IS NULL OR from_status IN ('PENDING_REVIEW', 'PENDING_ASSIGN', 'PENDING_ACCEPT', 'PROCESSING', 'COMPLETED', 'EVALUATED', 'REJECTED', 'CANCELLED')),
  CONSTRAINT ck_log_to_status CHECK (to_status IN ('PENDING_REVIEW', 'PENDING_ASSIGN', 'PENDING_ACCEPT', 'PROCESSING', 'COMPLETED', 'EVALUATED', 'REJECTED', 'CANCELLED')),
  INDEX idx_log_order_time (order_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  attachment_type VARCHAR(32) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  original_name VARCHAR(255),
  content_type VARCHAR(100),
  file_size BIGINT,
  uploaded_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_attachment_order FOREIGN KEY (order_id) REFERENCES repair_order(id),
  CONSTRAINT fk_attachment_user FOREIGN KEY (uploaded_by) REFERENCES sys_user(id),
  CONSTRAINT ck_attachment_type CHECK (attachment_type IN ('BEFORE_REPAIR', 'AFTER_REPAIR')),
  INDEX idx_attachment_order_type (order_id, attachment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_evaluation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  resident_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  content VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_evaluation_order FOREIGN KEY (order_id) REFERENCES repair_order(id),
  CONSTRAINT fk_evaluation_resident FOREIGN KEY (resident_id) REFERENCES sys_user(id),
  CONSTRAINT ck_evaluation_rating CHECK (rating BETWEEN 1 AND 5),
  INDEX idx_evaluation_resident (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  receiver_id BIGINT NOT NULL,
  order_id BIGINT,
  notification_type VARCHAR(40) NOT NULL,
  title VARCHAR(100) NOT NULL,
  content VARCHAR(500) NOT NULL,
  read_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES sys_user(id),
  CONSTRAINT fk_notification_order FOREIGN KEY (order_id) REFERENCES repair_order(id),
  INDEX idx_notification_receiver_time (receiver_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_role (id, role_code, role_name) VALUES
  (1, 'ADMIN', '管理员'),
  (2, 'RESIDENT', '居民'),
  (3, 'WORKER', '维修人员');

INSERT INTO sys_user (id, username, password_hash, real_name, phone, email) VALUES
  (1, 'admin', '{noop}123456', '系统管理员', '13800000001', 'admin@example.com'),
  (2, 'resident1', '{noop}123456', '居民一号', '13800000002', 'resident1@example.com'),
  (3, 'worker1', '{noop}123456', '维修师傅一号', '13800000003', 'worker1@example.com'),
  (4, 'worker2', '{noop}123456', '维修师傅二号', '13800000004', 'worker2@example.com');

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (1, 1),
  (2, 2),
  (3, 3),
  (4, 3);

INSERT INTO repair_category (id, category_code, category_name, description, sort_order, created_by) VALUES
  (1, 'ROAD', '道路破损', '道路坑洼、井盖松动、路面破损', 10, 1),
  (2, 'LIGHTING', '路灯照明', '路灯损坏、照明不足', 20, 1),
  (3, 'WATER', '供水排水', '漏水、堵塞、排水异常', 30, 1),
  (4, 'ENVIRONMENT', '环境卫生', '垃圾堆放、公共区域脏乱', 40, 1);

INSERT INTO repair_order (
  id, order_no, resident_id, category_id, title, location, description, contact_phone, priority, status
) VALUES (
  1, 'RO202606250001', 2, 1, '小区门口道路坑洼', '幸福小区南门', '南门出口路面有明显坑洼，雨天积水影响通行。', '13800000002', 'NORMAL', 'PENDING_REVIEW'
);

INSERT INTO order_status_log (order_id, operator_id, action, from_status, to_status, remark) VALUES
  (1, 2, 'CREATE', NULL, 'PENDING_REVIEW', '居民提交报修');
