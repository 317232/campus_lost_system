-- 数据库测试数据
-- 说明：
-- 1. 这里提供的是“数据库初始化测试数据”，不是 Service 层运行时临时拼装的假数据。
-- 2. Spring Boot 启动时会在 schema.sql 后执行本文件。
-- 3. 采用幂等 upsert / 条件插入，避免每次启动重复写入同一批样本。

-- =========================
-- RBAC 基础数据
-- =========================

INSERT INTO roles (role_code, role_name, description, is_system)
VALUES
    ('ADMIN', '系统管理员', '系统超级管理员，拥有所有权限', TRUE),
    ('USER', '普通用户', '普通用户，拥有基础业务权限', TRUE),
    ('AUDITOR', '审核员', '内容审核专员，负责审核物品与认领申请', TRUE)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    description = VALUES(description),
    is_system = VALUES(is_system);

INSERT INTO permissions (perm_code, perm_name, resource_type, resource_path, action, description)
VALUES
    ('item:create', '创建物品', 'item', '/api/items', 'POST', '发布失物或招领信息'),
    ('item:update', '更新物品', 'item', '/api/items/*', 'PUT', '修改物品信息'),
    ('item:delete', '删除物品', 'item', '/api/items/*', 'DELETE', '删除物品信息'),
    ('item:view', '查看物品', 'item', '/api/items/**', 'GET', '查看物品列表与详情'),
    ('item:unlock-contact', '解锁联系方式', 'item', '/api/items/*/contact-unlock', 'POST', '查看完整联系方式'),
    ('claim:create', '创建认领', 'claim', '/api/claims', 'POST', '提交认领申请'),
    ('claim:keyword-check', '关键词核验', 'claim', '/api/claims/keyword-check', 'POST', '认领前关键词核验'),
    ('audit:item', '审核物品', 'audit', '/api/admin/items/*/audit', 'PUT', '审核物品信息'),
    ('audit:claim', '审核认领', 'audit', '/api/admin/claims/*/audit', 'PUT', '审核认领申请'),
    ('audit:list', '查看审核列表', 'audit', '/api/admin/**', 'GET', '查看管理端审核列表'),
    ('user:view', '查看用户信息', 'user', '/api/users/me', 'GET', '查看个人资料与记录'),
    ('user:manage', '管理用户', 'user', '/api/admin/users/**', 'PUT', '管理用户状态'),
    ('category:view', '查看分类', 'category', '/api/categories', 'GET', '查看分类字典'),
    ('report:create', '创建举报', 'report', '/api/reports', 'POST', '提交举报反馈'),
    ('report:view', '查看举报', 'report', '/api/admin/reports', 'GET', '查看举报队列'),
    ('report:resolve', '处理举报', 'report', '/api/admin/reports/*/resolve', 'PUT', '处理举报反馈'),
    ('announcement:view', '查看公告', 'announcement', '/api/announcements/**', 'GET', '查看公告列表与详情'),
    ('announcement:create', '创建公告', 'announcement', '/api/admin/announcements', 'POST', '创建公告'),
    ('announcement:update', '更新公告', 'announcement', '/api/admin/announcements/*', 'PUT', '更新公告'),
    ('announcement:delete', '删除公告', 'announcement', '/api/admin/announcements/*', 'DELETE', '删除公告'),
    ('statistics:view', '查看统计', 'statistics', '/api/admin/statistics/**', 'GET', '查看平台统计数据'),
    ('file:upload', '文件上传', 'file', '/api/files/**', 'POST', '上传物品图片')
ON DUPLICATE KEY UPDATE
    perm_name = VALUES(perm_name),
    resource_type = VALUES(resource_type),
    resource_path = VALUES(resource_path),
    action = VALUES(action),
    description = VALUES(description);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.role_code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.perm_code IN (
    'item:create', 'item:update', 'item:delete', 'item:view', 'item:unlock-contact',
    'claim:create', 'claim:keyword-check',
    'user:view', 'category:view', 'report:create', 'announcement:view', 'file:upload'
)
WHERE r.role_code = 'USER'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.perm_code IN (
    'item:view', 'audit:item', 'audit:claim', 'audit:list',
    'user:view', 'category:view', 'report:view', 'announcement:view'
)
WHERE r.role_code = 'AUDITOR'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- =========================
-- 测试账号
-- 密码：
-- admin001 / admin123
-- auditor001 / 123456
-- 其他测试用户 / 123456
-- =========================

INSERT INTO users (student_no, name, password_hash, phone, email, major, avatar_url, status)
VALUES
    ('admin001', '系统管理员', '$2b$12$pPGkOVIfimBkZ9TTZCJubOMDkWn7RlAS2PwEPd2CYPsDFd0ZXa2z6', '18800009999', 'admin@example.com', '系统管理', NULL, 'NORMAL'),
    ('auditor001', '审核员小周', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800001001', 'auditor@example.com', '审核中心', NULL, 'NORMAL'),
    ('2023110421', '林知夏', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800001111', 'lin@example.com', '软件工程', NULL, 'NORMAL'),
    ('2023110999', '周叙白', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800002222', 'zhou@example.com', '网络工程', NULL, 'NORMAL'),
    ('2023110666', '王同学', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800003333', 'wang@example.com', '人工智能', NULL, 'NORMAL'),
    ('2023110777', '赵同学', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800004444', 'zhao@example.com', '自动化', NULL, 'NORMAL'),
    ('2023110888', '陈同学', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800005555', 'chen@example.com', '电子信息', NULL, 'NORMAL'),
    ('2023110555', '许同学', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800006666', 'xu@example.com', '数据科学', NULL, 'NORMAL'),
    ('2023110333', '李同学', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800007777', 'li@example.com', '机械工程', NULL, 'NORMAL'),
    ('2023110222', '匿名拾主', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800008888', 'finder@example.com', '计算机科学', NULL, 'NORMAL'),
    ('2023110111', '后勤点位', '$2b$12$oLr70F6ItCsvFo3FRckUoOx1T1N2wW5SnnVwm6bxtE7Vj/k0/pMs2', '18800009998', 'logistics@example.com', '后勤中心', NULL, 'NORMAL')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    password_hash = VALUES(password_hash),
    phone = VALUES(phone),
    email = VALUES(email),
    major = VALUES(major),
    avatar_url = VALUES(avatar_url),
    status = VALUES(status);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.role_code = 'ADMIN'
WHERE u.student_no = 'admin001'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.role_code = 'AUDITOR'
WHERE u.student_no = 'auditor001'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.role_code = 'USER'
WHERE u.student_no IN (
    '2023110421', '2023110999', '2023110666', '2023110777',
    '2023110888', '2023110555', '2023110333', '2023110222', '2023110111'
)
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- =========================
-- 分类测试数据
-- =========================

INSERT INTO categories (biz_id, name, sort_order, status)
VALUES
    ('cat-001', '证件', 10, 'ENABLED'),
    ('cat-002', '电子产品', 20, 'ENABLED'),
    ('cat-003', '书籍资料', 30, 'ENABLED'),
    ('cat-004', '钥匙卡证', 40, 'ENABLED'),
    ('cat-005', '衣物配饰', 50, 'ENABLED'),
    ('cat-006', '日用品', 60, 'ENABLED'),
    ('cat-007', '其他', 99, 'ENABLED')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order),
    status = VALUES(status);

-- =========================
-- 物品测试数据
-- =========================

INSERT INTO items (
    biz_id, scene, owner_id, category_id, title, item_name, occurred_area, occurred_location, occurred_at,
    verify_method, description, value_tag, contact_visibility, status, audit_status
)
SELECT
    'found-001', 'FOUND', u.id, c.id,
    '校园卡 · 信息工程楼附近', '校园卡', '教学楼',
    '信息楼 A 区', '2026-04-12 18:24:00',
    '卡套颜色 + 最近一次刷卡地点',
    '在信息工程楼与图书馆连廊之间拾到校园卡，适合用于认领流程联调。', 'HIGH', 'MASKED', 'PUBLISHED', 'APPROVED'
FROM users u
JOIN categories c ON c.name = '钥匙卡证'
WHERE u.student_no = '2023110999'
ON DUPLICATE KEY UPDATE
    scene = VALUES(scene),
    owner_id = VALUES(owner_id),
    category_id = VALUES(category_id),
    title = VALUES(title),
    item_name = VALUES(item_name),
    occurred_area = VALUES(occurred_area),
    occurred_location = VALUES(occurred_location),
    occurred_at = VALUES(occurred_at),
    verify_method = VALUES(verify_method),
    description = VALUES(description),
    value_tag = VALUES(value_tag),
    contact_visibility = VALUES(contact_visibility),
    status = VALUES(status),
    audit_status = VALUES(audit_status);

INSERT INTO items (
    biz_id, scene, owner_id, category_id, title, item_name, occurred_area, occurred_location, occurred_at,
    verify_method, description, value_tag, contact_visibility, status, audit_status
)
SELECT
    'found-002', 'FOUND', u.id, c.id,
    '银色钥匙串', '钥匙串', '教学楼',
    '实验楼门口', '2026-04-12 15:10:00',
    '钥匙数量 + 是否有门禁扣',
    '钥匙串包含多把钥匙，适合做普通招领信息展示。', NULL, 'MASKED', 'PUBLISHED', 'APPROVED'
FROM users u
JOIN categories c ON c.name = '钥匙卡证'
WHERE u.student_no = '2023110777'
ON DUPLICATE KEY UPDATE
    scene = VALUES(scene),
    owner_id = VALUES(owner_id),
    category_id = VALUES(category_id),
    title = VALUES(title),
    item_name = VALUES(item_name),
    occurred_area = VALUES(occurred_area),
    occurred_location = VALUES(occurred_location),
    occurred_at = VALUES(occurred_at),
    verify_method = VALUES(verify_method),
    description = VALUES(description),
    value_tag = VALUES(value_tag),
    contact_visibility = VALUES(contact_visibility),
    status = VALUES(status),
    audit_status = VALUES(audit_status);

INSERT INTO items (
    biz_id, scene, owner_id, category_id, title, item_name, occurred_area, occurred_location, occurred_at,
    verify_method, description, value_tag, contact_visibility, status, audit_status
)
SELECT
    'lost-001', 'LOST', u.id, c.id,
    '学生证急寻', '学生证', '教学楼',
    '主教到图书馆沿线', '2026-04-12 08:05:00',
    '姓名首字母 + 班级简称',
    '失主在主教到图书馆沿线丢失学生证，用于失物侧详情与推荐联调。', NULL, 'MASKED', 'PUBLISHED', 'APPROVED'
FROM users u
JOIN categories c ON c.name = '证件'
WHERE u.student_no = '2023110421'
ON DUPLICATE KEY UPDATE
    scene = VALUES(scene),
    owner_id = VALUES(owner_id),
    category_id = VALUES(category_id),
    title = VALUES(title),
    item_name = VALUES(item_name),
    occurred_area = VALUES(occurred_area),
    occurred_location = VALUES(occurred_location),
    occurred_at = VALUES(occurred_at),
    verify_method = VALUES(verify_method),
    description = VALUES(description),
    value_tag = VALUES(value_tag),
    contact_visibility = VALUES(contact_visibility),
    status = VALUES(status),
    audit_status = VALUES(audit_status);

INSERT INTO items (
    biz_id, scene, owner_id, category_id, title, item_name, occurred_area, occurred_location, occurred_at,
    verify_method, description, value_tag, contact_visibility, status, audit_status
)
SELECT
    'lost-002', 'LOST', u.id, c.id,
    '深灰色平板电脑丢失', '平板电脑', '图书馆',
    '图书馆三层自习区', '2026-04-11 21:10:00',
    '保护壳颜色 + 锁屏壁纸',
    '用于管理员失物待审核列表中的样本数据。', 'HIGH', 'MASKED', 'PENDING_REVIEW', 'PENDING'
FROM users u
JOIN categories c ON c.name = '电子产品'
WHERE u.student_no = '2023110666'
ON DUPLICATE KEY UPDATE
    scene = VALUES(scene),
    owner_id = VALUES(owner_id),
    category_id = VALUES(category_id),
    title = VALUES(title),
    item_name = VALUES(item_name),
    occurred_area = VALUES(occurred_area),
    occurred_location = VALUES(occurred_location),
    occurred_at = VALUES(occurred_at),
    verify_method = VALUES(verify_method),
    description = VALUES(description),
    value_tag = VALUES(value_tag),
    contact_visibility = VALUES(contact_visibility),
    status = VALUES(status),
    audit_status = VALUES(audit_status);

INSERT INTO items (
    biz_id, scene, owner_id, category_id, title, item_name, occurred_area, occurred_location, occurred_at,
    verify_method, description, value_tag, contact_visibility, status, audit_status
)
SELECT
    'found-003', 'FOUND', u.id, c.id,
    '蓝牙耳机', '蓝牙耳机', '教学楼',
    '二教 104', '2026-04-11 10:30:00',
    '设备名称 + 配对手机型号',
    '该记录已完成认领，用于闭环状态验证。', NULL, 'MASKED', 'CLAIMED', 'APPROVED'
FROM users u
JOIN categories c ON c.name = '电子产品'
WHERE u.student_no = '2023110888'
ON DUPLICATE KEY UPDATE
    scene = VALUES(scene),
    owner_id = VALUES(owner_id),
    category_id = VALUES(category_id),
    title = VALUES(title),
    item_name = VALUES(item_name),
    occurred_area = VALUES(occurred_area),
    occurred_location = VALUES(occurred_location),
    occurred_at = VALUES(occurred_at),
    verify_method = VALUES(verify_method),
    description = VALUES(description),
    value_tag = VALUES(value_tag),
    contact_visibility = VALUES(contact_visibility),
    status = VALUES(status),
    audit_status = VALUES(audit_status);

INSERT INTO items (
    biz_id, scene, owner_id, category_id, title, item_name, occurred_area, occurred_location, occurred_at,
    verify_method, description, value_tag, contact_visibility, status, audit_status
)
SELECT
    'found-004', 'FOUND', u.id, c.id,
    '黑色保温杯', '保温杯', '宿舍区',
    '一食堂二层', '2026-04-12 09:16:00',
    '杯身贴纸 + 品牌',
    '用于管理员招领待审核列表中的样本数据。', NULL, 'MASKED', 'PENDING_REVIEW', 'PENDING'
FROM users u
JOIN categories c ON c.name = '日用品'
WHERE u.student_no = '2023110111'
ON DUPLICATE KEY UPDATE
    scene = VALUES(scene),
    owner_id = VALUES(owner_id),
    category_id = VALUES(category_id),
    title = VALUES(title),
    item_name = VALUES(item_name),
    occurred_area = VALUES(occurred_area),
    occurred_location = VALUES(occurred_location),
    occurred_at = VALUES(occurred_at),
    verify_method = VALUES(verify_method),
    description = VALUES(description),
    value_tag = VALUES(value_tag),
    contact_visibility = VALUES(contact_visibility),
    status = VALUES(status),
    audit_status = VALUES(audit_status);

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value)
SELECT i.id, 'WECHAT', '微信：signal-card-24', '点击登录后查看'
FROM items i
WHERE i.biz_id = 'found-001'
ON DUPLICATE KEY UPDATE
    contact_type = VALUES(contact_type),
    contact_value = VALUES(contact_value),
    masked_value = VALUES(masked_value);

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value)
SELECT i.id, 'QQ', 'QQ：2143321', '点击登录后查看'
FROM items i
WHERE i.biz_id = 'found-002'
ON DUPLICATE KEY UPDATE
    contact_type = VALUES(contact_type),
    contact_value = VALUES(contact_value),
    masked_value = VALUES(masked_value);

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value)
SELECT i.id, 'WECHAT', '微信：lin-zx-card', '点击登录后查看'
FROM items i
WHERE i.biz_id = 'lost-001'
ON DUPLICATE KEY UPDATE
    contact_type = VALUES(contact_type),
    contact_value = VALUES(contact_value),
    masked_value = VALUES(masked_value);

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value)
SELECT i.id, 'PHONE', '18800005555', '流程已闭环'
FROM items i
WHERE i.biz_id = 'found-003'
ON DUPLICATE KEY UPDATE
    contact_type = VALUES(contact_type),
    contact_value = VALUES(contact_value),
    masked_value = VALUES(masked_value);

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value)
SELECT i.id, 'DESK', '后勤失物点：A03', '点击登录后查看'
FROM items i
WHERE i.biz_id = 'found-004'
ON DUPLICATE KEY UPDATE
    contact_type = VALUES(contact_type),
    contact_value = VALUES(contact_value),
    masked_value = VALUES(masked_value);

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value)
SELECT i.id, 'PHONE', '18800003333', '点击登录后查看'
FROM items i
WHERE i.biz_id = 'lost-002'
ON DUPLICATE KEY UPDATE
    contact_type = VALUES(contact_type),
    contact_value = VALUES(contact_value),
    masked_value = VALUES(masked_value);

INSERT INTO item_attachments (item_id, file_url, file_type, sort_order)
SELECT i.id, '/uploads/demo-campus-card-1.jpg', 'image/jpeg', 0
FROM items i
WHERE i.biz_id = 'found-001'
  AND NOT EXISTS (
      SELECT 1 FROM item_attachments a
      WHERE a.item_id = i.id AND a.file_url = '/uploads/demo-campus-card-1.jpg'
  );

INSERT INTO item_attachments (item_id, file_url, file_type, sort_order)
SELECT i.id, '/uploads/demo-campus-card-2.jpg', 'image/jpeg', 1
FROM items i
WHERE i.biz_id = 'found-001'
  AND NOT EXISTS (
      SELECT 1 FROM item_attachments a
      WHERE a.item_id = i.id AND a.file_url = '/uploads/demo-campus-card-2.jpg'
  );

INSERT INTO item_attachments (item_id, file_url, file_type, sort_order)
SELECT i.id, '/uploads/demo-keys-1.jpg', 'image/jpeg', 0
FROM items i
WHERE i.biz_id = 'found-002'
  AND NOT EXISTS (
      SELECT 1 FROM item_attachments a
      WHERE a.item_id = i.id AND a.file_url = '/uploads/demo-keys-1.jpg'
  );

INSERT INTO item_attachments (item_id, file_url, file_type, sort_order)
SELECT i.id, '/uploads/demo-tablet-1.jpg', 'image/jpeg', 0
FROM items i
WHERE i.biz_id = 'lost-002'
  AND NOT EXISTS (
      SELECT 1 FROM item_attachments a
      WHERE a.item_id = i.id AND a.file_url = '/uploads/demo-tablet-1.jpg'
  );

INSERT INTO item_attachments (item_id, file_url, file_type, sort_order)
SELECT i.id, '/uploads/demo-cup-1.jpg', 'image/jpeg', 0
FROM items i
WHERE i.biz_id = 'found-004'
  AND NOT EXISTS (
      SELECT 1 FROM item_attachments a
      WHERE a.item_id = i.id AND a.file_url = '/uploads/demo-cup-1.jpg'
  );

INSERT INTO item_audits (item_id, audit_status, audit_remark, auditor_id)
SELECT i.id, 'APPROVED', '证件类招领信息通过审核', admin.id
FROM items i
JOIN users admin ON admin.student_no = 'admin001'
WHERE i.biz_id = 'found-001'
  AND NOT EXISTS (
      SELECT 1 FROM item_audits ia
      WHERE ia.item_id = i.id AND ia.audit_status = 'APPROVED' AND COALESCE(ia.audit_remark, '') = '证件类招领信息通过审核'
  );

INSERT INTO item_audits (item_id, audit_status, audit_remark, auditor_id)
SELECT i.id, 'APPROVED', '钥匙类招领信息通过审核', admin.id
FROM items i
JOIN users admin ON admin.student_no = 'admin001'
WHERE i.biz_id = 'found-002'
  AND NOT EXISTS (
      SELECT 1 FROM item_audits ia
      WHERE ia.item_id = i.id AND ia.audit_status = 'APPROVED' AND COALESCE(ia.audit_remark, '') = '钥匙类招领信息通过审核'
  );

INSERT INTO item_audits (item_id, audit_status, audit_remark, auditor_id)
SELECT i.id, 'APPROVED', '失物信息通过审核', admin.id
FROM items i
JOIN users admin ON admin.student_no = 'admin001'
WHERE i.biz_id = 'lost-001'
  AND NOT EXISTS (
      SELECT 1 FROM item_audits ia
      WHERE ia.item_id = i.id AND ia.audit_status = 'APPROVED' AND COALESCE(ia.audit_remark, '') = '失物信息通过审核'
  );

INSERT INTO item_audits (item_id, audit_status, audit_remark, auditor_id)
SELECT i.id, 'APPROVED', '闭环样例保留展示', admin.id
FROM items i
JOIN users admin ON admin.student_no = 'admin001'
WHERE i.biz_id = 'found-003'
  AND NOT EXISTS (
      SELECT 1 FROM item_audits ia
      WHERE ia.item_id = i.id AND ia.audit_status = 'APPROVED' AND COALESCE(ia.audit_remark, '') = '闭环样例保留展示'
  );

INSERT INTO item_audits (item_id, audit_status, audit_remark, auditor_id)
SELECT i.id, 'PENDING', NULL, admin.id
FROM items i
JOIN users admin ON admin.student_no = 'admin001'
WHERE i.biz_id IN ('lost-002', 'found-004')
  AND NOT EXISTS (
      SELECT 1 FROM item_audits ia
      WHERE ia.item_id = i.id AND ia.audit_status = 'PENDING'
  );

-- =========================
-- 认领测试数据
-- =========================

INSERT INTO claim_drafts (draft_token, item_id, claimant_id, keyword_proof, status, expires_at, used_at)
SELECT
    'claim-draft-demo-001',
    i.id,
    u.id,
    '卡套是浅蓝色，最近一次在信息楼刷卡',
    'USED',
    '2026-04-12 19:00:00',
    '2026-04-12 18:40:00'
FROM items i
JOIN users u ON u.student_no = '2023110421'
WHERE i.biz_id = 'found-001'
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    claimant_id = VALUES(claimant_id),
    keyword_proof = VALUES(keyword_proof),
    status = VALUES(status),
    expires_at = VALUES(expires_at),
    used_at = VALUES(used_at);

INSERT INTO claim_drafts (draft_token, item_id, claimant_id, keyword_proof, status, expires_at, used_at)
SELECT
    'claim-draft-demo-002',
    i.id,
    u.id,
    '三把钥匙，有门禁扣',
    'ACTIVE',
    '2026-04-12 20:00:00',
    NULL
FROM items i
JOIN users u ON u.student_no = '2023110555'
WHERE i.biz_id = 'found-002'
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    claimant_id = VALUES(claimant_id),
    keyword_proof = VALUES(keyword_proof),
    status = VALUES(status),
    expires_at = VALUES(expires_at),
    used_at = VALUES(used_at);

INSERT INTO claims (biz_id, item_id, claimant_id, draft_id, formal_proof, contact_info, status, audit_remark)
SELECT
    'claim-9001',
    i.id,
    u.id,
    d.id,
    '补充班级与最近一次刷卡地点',
    '18800001111',
    'PENDING',
    NULL
FROM items i
JOIN users u ON u.student_no = '2023110421'
JOIN claim_drafts d ON d.draft_token = 'claim-draft-demo-001'
WHERE i.biz_id = 'found-001'
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    claimant_id = VALUES(claimant_id),
    draft_id = VALUES(draft_id),
    formal_proof = VALUES(formal_proof),
    contact_info = VALUES(contact_info),
    status = VALUES(status),
    audit_remark = VALUES(audit_remark);

INSERT INTO claims (biz_id, item_id, claimant_id, draft_id, formal_proof, contact_info, status, audit_remark)
SELECT
    'claim-9002',
    i.id,
    u.id,
    NULL,
    '设备名称和配对手机信息已核验',
    '18800002222',
    'APPROVED',
    '管理员已确认认领'
FROM items i
JOIN users u ON u.student_no = '2023110999'
WHERE i.biz_id = 'found-003'
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    claimant_id = VALUES(claimant_id),
    formal_proof = VALUES(formal_proof),
    contact_info = VALUES(contact_info),
    status = VALUES(status),
    audit_remark = VALUES(audit_remark);

INSERT INTO claims (biz_id, item_id, claimant_id, draft_id, formal_proof, contact_info, status, audit_remark)
SELECT
    'claim-9003',
    i.id,
    u.id,
    NULL,
    '只记得大概地点，无法补充更多细节',
    '18800006666',
    'REJECTED',
    '证明信息不足，无法确认归属'
FROM items i
JOIN users u ON u.student_no = '2023110555'
WHERE i.biz_id = 'found-001'
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    claimant_id = VALUES(claimant_id),
    formal_proof = VALUES(formal_proof),
    contact_info = VALUES(contact_info),
    status = VALUES(status),
    audit_remark = VALUES(audit_remark);

INSERT INTO claim_audits (claim_id, audit_status, audit_remark, auditor_id)
SELECT c.id, 'PENDING', NULL, admin.id
FROM claims c
JOIN users admin ON admin.student_no = 'admin001'
WHERE c.biz_id = 'claim-9001'
  AND NOT EXISTS (
      SELECT 1 FROM claim_audits ca
      WHERE ca.claim_id = c.id AND ca.audit_status = 'PENDING'
  );

INSERT INTO claim_audits (claim_id, audit_status, audit_remark, auditor_id)
SELECT c.id, 'APPROVED', '管理员已确认认领', admin.id
FROM claims c
JOIN users admin ON admin.student_no = 'admin001'
WHERE c.biz_id = 'claim-9002'
  AND NOT EXISTS (
      SELECT 1 FROM claim_audits ca
      WHERE ca.claim_id = c.id AND ca.audit_status = 'APPROVED' AND COALESCE(ca.audit_remark, '') = '管理员已确认认领'
  );

INSERT INTO claim_audits (claim_id, audit_status, audit_remark, auditor_id)
SELECT c.id, 'REJECTED', '证明信息不足，无法确认归属', admin.id
FROM claims c
JOIN users admin ON admin.student_no = 'admin001'
WHERE c.biz_id = 'claim-9003'
  AND NOT EXISTS (
      SELECT 1 FROM claim_audits ca
      WHERE ca.claim_id = c.id AND ca.audit_status = 'REJECTED' AND COALESCE(ca.audit_remark, '') = '证明信息不足，无法确认归属'
  );

-- =========================
-- 公告测试数据
-- =========================

INSERT INTO announcements (biz_id, title, content, status, publisher_id, published_at)
SELECT
    'announcement-001',
    '失物认领高峰时段提醒',
    '本周图书馆、信息楼与食堂区域失物信息较多，请同学们在认领前尽量补充可验证特征，避免误认领。',
    'PUBLISHED',
    u.id,
    '2026-04-12 12:00:00'
FROM users u
WHERE u.student_no = 'admin001'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    publisher_id = VALUES(publisher_id),
    published_at = VALUES(published_at);

INSERT INTO announcements (biz_id, title, content, status, publisher_id, published_at)
SELECT
    'announcement-002',
    '高价值物品统一交接说明',
    '校园卡、证件、数码设备等高价值物品将优先进入审核流程，管理员审核通过后才会对外展示。',
    'PUBLISHED',
    u.id,
    '2026-04-11 09:00:00'
FROM users u
WHERE u.student_no = 'admin001'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    publisher_id = VALUES(publisher_id),
    published_at = VALUES(published_at);

INSERT INTO announcements (biz_id, title, content, status, publisher_id, published_at)
SELECT
    'announcement-003',
    '旧学期公告归档样例',
    '这是一条下线状态的测试公告，用于管理端验证公告上下线和筛选能力。',
    'OFFLINE',
    u.id,
    NULL
FROM users u
WHERE u.student_no = 'admin001'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    publisher_id = VALUES(publisher_id),
    published_at = VALUES(published_at);

-- =========================
-- 举报/反馈测试数据
-- =========================

INSERT INTO reports (
    biz_id, reporter_id, target_type, target_id, reason, detail, status, handled_by, handled_at, handle_remark
)
SELECT
    'report-001',
    reporter.id,
    'ITEM',
    i.id,
    '疑似重复发布',
    '该招领信息与另一条记录文案高度相似，怀疑重复提交。',
    'PENDING',
    NULL,
    NULL,
    NULL
FROM users reporter
JOIN items i ON i.biz_id = 'found-004'
WHERE reporter.student_no = '2023110666'
ON DUPLICATE KEY UPDATE
    reporter_id = VALUES(reporter_id),
    target_type = VALUES(target_type),
    target_id = VALUES(target_id),
    reason = VALUES(reason),
    detail = VALUES(detail),
    status = VALUES(status),
    handled_by = VALUES(handled_by),
    handled_at = VALUES(handled_at),
    handle_remark = VALUES(handle_remark);

INSERT INTO reports (
    biz_id, reporter_id, target_type, target_id, reason, detail, status, handled_by, handled_at, handle_remark
)
SELECT
    'report-002',
    reporter.id,
    'ITEM',
    i.id,
    '联系方式疑似无效',
    '尝试通过展示的联系方式联系发布人未得到回应，申请管理员复核。',
    'PROCESSING',
    admin.id,
    '2026-04-12 16:00:00',
    '已联系发布人补充有效联系方式。'
FROM users reporter
JOIN users admin ON admin.student_no = 'admin001'
JOIN items i ON i.biz_id = 'lost-001'
WHERE reporter.student_no = '2023110555'
ON DUPLICATE KEY UPDATE
    reporter_id = VALUES(reporter_id),
    target_type = VALUES(target_type),
    target_id = VALUES(target_id),
    reason = VALUES(reason),
    detail = VALUES(detail),
    status = VALUES(status),
    handled_by = VALUES(handled_by),
    handled_at = VALUES(handled_at),
    handle_remark = VALUES(handle_remark);

INSERT INTO reports (
    biz_id, reporter_id, target_type, target_id, reason, detail, status, handled_by, handled_at, handle_remark
)
SELECT
    'report-003',
    reporter.id,
    'ANNOUNCEMENT',
    a.id,
    '公告信息过期',
    '该公告已不再适用，建议管理员归档或下线。',
    'RESOLVED',
    admin.id,
    '2026-04-12 14:00:00',
    '已确认该公告为归档样例，无需继续展示到前台。'
FROM users reporter
JOIN users admin ON admin.student_no = 'admin001'
JOIN announcements a ON a.biz_id = 'announcement-003'
WHERE reporter.student_no = '2023110333'
ON DUPLICATE KEY UPDATE
    reporter_id = VALUES(reporter_id),
    target_type = VALUES(target_type),
    target_id = VALUES(target_id),
    reason = VALUES(reason),
    detail = VALUES(detail),
    status = VALUES(status),
    handled_by = VALUES(handled_by),
    handled_at = VALUES(handled_at),
    handle_remark = VALUES(handle_remark);

-- =========================
-- 辅助日志数据
-- =========================

INSERT INTO contact_unlock_logs (item_id, viewer_id, source)
SELECT i.id, u.id, 'DETAIL_PAGE'
FROM items i
JOIN users u ON u.student_no = '2023110421'
WHERE i.biz_id = 'found-001'
  AND NOT EXISTS (
      SELECT 1 FROM contact_unlock_logs l
      WHERE l.item_id = i.id AND l.viewer_id = u.id AND l.source = 'DETAIL_PAGE'
  );

INSERT INTO contact_unlock_logs (item_id, viewer_id, source)
SELECT i.id, u.id, 'DETAIL_PAGE'
FROM items i
JOIN users u ON u.student_no = '2023110555'
WHERE i.biz_id = 'found-002'
  AND NOT EXISTS (
      SELECT 1 FROM contact_unlock_logs l
      WHERE l.item_id = i.id AND l.viewer_id = u.id AND l.source = 'DETAIL_PAGE'
  );

INSERT INTO operation_logs (operator_id, target_type, target_id, action, from_status, to_status, remark)
SELECT u.id, 'ITEM', i.id, 'PUBLISH', NULL, 'PUBLISHED', '发布失物信息：学生证急寻'
FROM users u
JOIN items i ON i.biz_id = 'lost-001'
WHERE u.student_no = '2023110421'
  AND NOT EXISTS (
      SELECT 1 FROM operation_logs l
      WHERE l.operator_id = u.id AND l.target_type = 'ITEM' AND l.target_id = i.id AND l.action = 'PUBLISH'
  );

INSERT INTO operation_logs (operator_id, target_type, target_id, action, from_status, to_status, remark)
SELECT u.id, 'ITEM', i.id, 'PUBLISH', NULL, 'PUBLISHED', '发布招领信息：校园卡 · 信息工程楼附近'
FROM users u
JOIN items i ON i.biz_id = 'found-001'
WHERE u.student_no = '2023110999'
  AND NOT EXISTS (
      SELECT 1 FROM operation_logs l
      WHERE l.operator_id = u.id AND l.target_type = 'ITEM' AND l.target_id = i.id AND l.action = 'PUBLISH'
  );

INSERT INTO operation_logs (operator_id, target_type, target_id, action, from_status, to_status, remark)
SELECT u.id, 'CLAIM', c.id, 'SUBMIT', NULL, 'PENDING', '提交认领申请：claim-9001'
FROM users u
JOIN claims c ON c.biz_id = 'claim-9001'
WHERE u.student_no = '2023110421'
  AND NOT EXISTS (
      SELECT 1 FROM operation_logs l
      WHERE l.operator_id = u.id AND l.target_type = 'CLAIM' AND l.target_id = c.id AND l.action = 'SUBMIT'
  );

INSERT INTO operation_logs (operator_id, target_type, target_id, action, from_status, to_status, remark)
SELECT u.id, 'CLAIM', c.id, 'AUDIT', 'PENDING', 'APPROVED', '管理员审核通过认领'
FROM users u
JOIN claims c ON c.biz_id = 'claim-9002'
WHERE u.student_no = 'admin001'
  AND NOT EXISTS (
      SELECT 1 FROM operation_logs l
      WHERE l.operator_id = u.id AND l.target_type = 'CLAIM' AND l.target_id = c.id AND l.action = 'AUDIT'
  );

INSERT INTO operation_logs (operator_id, target_type, target_id, action, from_status, to_status, remark)
SELECT u.id, 'REPORT', r.id, 'RESOLVE', 'PROCESSING', 'RESOLVED', '管理员处理过期公告举报'
FROM users u
JOIN reports r ON r.biz_id = 'report-003'
WHERE u.student_no = 'admin001'
  AND NOT EXISTS (
      SELECT 1 FROM operation_logs l
      WHERE l.operator_id = u.id AND l.target_type = 'REPORT' AND l.target_id = r.id AND l.action = 'RESOLVE'
  );
