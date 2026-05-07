-- init_optimized.sql
-- Campus Lost & Found System
-- MySQL 8.0 / InnoDB / utf8mb4
-- 设计目标：
-- 1) 按需求文档覆盖用户、物品发布、搜索筛选、认领审核、公告、举报反馈、统计等核心功能；
-- 2) 满足第三范式：分类、用户、角色、权限、联系方式、附件、审核历史独立建表；
-- 3) 物品状态只使用 items.status 表达业务状态，删除原 stage，避免阶段字段与状态字段重复；
-- 4) 认领流程采用：申请 APPLIED -> 描述核对 CHECKING/REVIEW_PENDING -> 审核通过 APPROVED，并由业务事务同步更新 items.status=CLAIMED；
-- 5) 所有表统一使用 create_time/update_time/is_delete，逻辑删除统一由 is_delete 控制。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS contact_unlock_logs;
DROP TABLE IF EXISTS operation_logs;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS claim_audits;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS item_audits;
DROP TABLE IF EXISTS item_attachments;
DROP TABLE IF EXISTS item_contacts;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================
-- 1. 用户、角色、权限
-- =====================================

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    student_no VARCHAR(32) NOT NULL COMMENT '学号/账号，唯一身份标识',
    name VARCHAR(64) NOT NULL COMMENT '姓名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    major VARCHAR(128) DEFAULT NULL COMMENT '专业',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE/DISABLED',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_student_no UNIQUE (student_no),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    KEY idx_users_status (status),
    KEY idx_users_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE roles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(32) NOT NULL COMMENT '角色编码：ADMIN/USER',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统内置角色',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_role_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE permissions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    perm_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    perm_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型：MENU/API/BUTTON/DATA',
    resource_path VARCHAR(255) NOT NULL COMMENT '资源路径',
    action VARCHAR(32) NOT NULL COMMENT '操作类型：CREATE/READ/UPDATE/DELETE/AUDIT/MANAGE',
    description VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_permissions_perm_code UNIQUE (perm_code),
    KEY idx_permissions_resource (resource_type, resource_path),
    KEY idx_permissions_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE user_roles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户角色关联ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    KEY idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE role_permissions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色权限关联ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_role_permissions_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    KEY idx_role_permissions_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================
-- 2. 基础分类
-- =====================================

CREATE TABLE categories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    biz_id VARCHAR(32) NOT NULL COMMENT '分类业务编号',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_biz_id UNIQUE (biz_id),
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT ck_categories_status CHECK (status IN ('ENABLED', 'DISABLED')),
    KEY idx_categories_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- =====================================
-- 3. 物品管理
-- =====================================

CREATE TABLE items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    biz_id VARCHAR(32) NOT NULL COMMENT '业务编号',
    scene VARCHAR(16) NOT NULL COMMENT '场景：LOST/FOUND，分别表示失物/招领',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态：PENDING_REVIEW/PUBLISHED/REJECTED/CLAIMING/CLAIMED/FOUND_BACK/OFFLINE',
    owner_id BIGINT UNSIGNED NOT NULL COMMENT '发布人ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    location VARCHAR(255) NOT NULL COMMENT '丢失/拾取地点',
    occurred_at DATETIME NOT NULL COMMENT '丢失/拾取时间',
    verify_method VARCHAR(255) DEFAULT NULL COMMENT '认领验证方式，主要用于招领信息',
    description TEXT DEFAULT NULL COMMENT '物品描述',
    value_tag VARCHAR(16) DEFAULT NULL COMMENT '价值标签：LOW/MEDIUM/HIGH',
    contact_visibility VARCHAR(16) NOT NULL DEFAULT 'UNMASKED' COMMENT '联系方式可见性：MASKED/UNMASKED',
    published_at DATETIME DEFAULT NULL COMMENT '发布时间',
    closed_at DATETIME DEFAULT NULL COMMENT '关闭/完成时间',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_items_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT ck_items_scene CHECK (scene IN ('LOST', 'FOUND')),
    CONSTRAINT ck_items_status CHECK (status IN ('PENDING_REVIEW', 'PUBLISHED', 'REJECTED', 'CLAIMING', 'CLAIMED', 'FOUND_BACK', 'OFFLINE')),
    CONSTRAINT ck_items_contact_visibility CHECK (contact_visibility IN ('MASKED', 'UNMASKED')),
    CONSTRAINT ck_items_value_tag CHECK (value_tag IS NULL OR value_tag IN ('LOW', 'MEDIUM', 'HIGH')),
    KEY idx_items_owner_id (owner_id),
    KEY idx_items_category_id (category_id),
    KEY idx_items_scene_status (scene, status),
    KEY idx_items_status_created (status, create_time),
    KEY idx_items_public_query (is_delete, scene, status, category_id, occurred_at, create_time),
    KEY idx_items_item_name (item_name),
    KEY idx_items_location (location(64)),
    KEY idx_items_occurred_at (occurred_at),
    KEY idx_items_is_delete (is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品主表：统一存储失物与招领信息';

CREATE TABLE item_contacts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '联系方式ID',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '物品ID',
    contact_type VARCHAR(32) NOT NULL COMMENT '联系方式类型：PHONE/EMAIL/WECHAT/QQ/OTHER',
    contact_value VARCHAR(255) NOT NULL COMMENT '联系方式内容',
    masked_value VARCHAR(255) NOT NULL COMMENT '脱敏联系方式',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否主联系方式',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_item_contacts_item_type UNIQUE (item_id, contact_type),
    CONSTRAINT fk_item_contacts_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT ck_item_contacts_type CHECK (contact_type IN ('PHONE', 'EMAIL', 'WECHAT', 'QQ', 'OTHER')),
    KEY idx_item_contacts_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品联系方式表';

CREATE TABLE item_attachments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '物品ID',
    file_url VARCHAR(255) NOT NULL COMMENT '文件访问路径',
    file_name VARCHAR(128) DEFAULT NULL COMMENT '原始文件名',
    file_type VARCHAR(32) NOT NULL DEFAULT 'IMAGE' COMMENT '文件类型：IMAGE/DOCUMENT/OTHER',
    file_size BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小，单位字节',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_item_attachments_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT ck_item_attachments_type CHECK (file_type IN ('IMAGE', 'DOCUMENT', 'OTHER')),
    KEY idx_item_attachments_item_sort (item_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品附件表';

CREATE TABLE item_audits (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物品审核记录ID',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '物品ID',
    audit_action VARCHAR(32) NOT NULL COMMENT '审核操作：APPROVE/REJECT/OFFLINE/RESTORE/DELETE',
    from_status VARCHAR(32) DEFAULT NULL COMMENT '审核前状态',
    to_status VARCHAR(32) NOT NULL COMMENT '审核后状态',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    auditor_id BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_item_audits_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_audits_auditor FOREIGN KEY (auditor_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT ck_item_audits_action CHECK (audit_action IN ('APPROVE', 'REJECT', 'OFFLINE', 'RESTORE', 'DELETE')),
    KEY idx_item_audits_item_created (item_id, create_time),
    KEY idx_item_audits_auditor_created (auditor_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品审核历史表';

-- =====================================
-- 4. 认领管理：申请 -> 描述核对 -> 审核通过
-- =====================================

CREATE TABLE claims (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '认领申请ID',
    biz_id VARCHAR(32) NOT NULL COMMENT '认领业务编号',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '被认领物品ID，仅招领信息允许认领',
    claimant_id BIGINT UNSIGNED NOT NULL COMMENT '认领人ID',
    claim_statement TEXT NOT NULL COMMENT '认领说明',
    feature_proof TEXT NOT NULL COMMENT '物品特征证明信息',
    contact VARCHAR(255) NOT NULL COMMENT '认领人联系信息',
    status VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT '状态：APPLIED/CHECKING/REVIEW_PENDING/APPROVED/REJECTED/CANCELLED',
    description_check_result VARCHAR(16) DEFAULT NULL COMMENT '描述核对结果：MATCHED/NOT_MATCHED/UNCERTAIN',
    description_checked_by BIGINT UNSIGNED DEFAULT NULL COMMENT '描述核对人ID',
    description_checked_at DATETIME DEFAULT NULL COMMENT '描述核对时间',
    reviewed_by BIGINT UNSIGNED DEFAULT NULL COMMENT '最终审核人ID',
    reviewed_at DATETIME DEFAULT NULL COMMENT '最终审核时间',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_claims_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_claims_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_checked_by FOREIGN KEY (description_checked_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_claims_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT ck_claims_status CHECK (status IN ('APPLIED', 'CHECKING', 'REVIEW_PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT ck_claims_check_result CHECK (description_check_result IS NULL OR description_check_result IN ('MATCHED', 'NOT_MATCHED', 'UNCERTAIN')),
    KEY idx_claims_item_status (item_id, status),
    KEY idx_claims_claimant_status (claimant_id, status),
    KEY idx_claims_status_created (status, create_time),
    KEY idx_claims_checked_by (description_checked_by),
    KEY idx_claims_reviewed_by (reviewed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认领申请表';

CREATE TABLE claim_audits (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '认领审核记录ID',
    claim_id BIGINT UNSIGNED NOT NULL COMMENT '认领申请ID',
    audit_action VARCHAR(32) NOT NULL COMMENT '操作：START_CHECK/CHECK_PASS/CHECK_FAIL/SUBMIT_REVIEW/APPROVE/REJECT/CANCEL',
    from_status VARCHAR(32) DEFAULT NULL COMMENT '操作前状态',
    to_status VARCHAR(32) NOT NULL COMMENT '操作后状态',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '操作备注',
    auditor_id BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_claim_audits_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_audits_auditor FOREIGN KEY (auditor_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT ck_claim_audits_action CHECK (audit_action IN ('START_CHECK', 'CHECK_PASS', 'CHECK_FAIL', 'SUBMIT_REVIEW', 'APPROVE', 'REJECT', 'CANCEL')),
    KEY idx_claim_audits_claim_created (claim_id, create_time),
    KEY idx_claim_audits_auditor_created (auditor_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认领审核历史表';

-- =====================================
-- 5. 公告与举报反馈
-- =====================================

CREATE TABLE announcements (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    biz_id VARCHAR(32) NOT NULL COMMENT '公告业务编号',
    title VARCHAR(120) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/OFFLINE',
    publisher_id BIGINT UNSIGNED NOT NULL COMMENT '发布人ID',
    published_at DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_announcements_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_announcements_publisher FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_announcements_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'OFFLINE')),
    KEY idx_announcements_status_published (status, published_at),
    KEY idx_announcements_publisher_created (publisher_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

CREATE TABLE reports (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报/反馈ID',
    biz_id VARCHAR(32) NOT NULL COMMENT '举报/反馈业务编号',
    reporter_id BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
    report_type VARCHAR(16) NOT NULL COMMENT '类型：REPORT/FEEDBACK',
    target_type VARCHAR(32) DEFAULT NULL COMMENT '对象类型：ITEM/CLAIM/ANNOUNCEMENT/USER，反馈可为空',
    target_id BIGINT UNSIGNED DEFAULT NULL COMMENT '对象ID，反馈可为空',
    reason VARCHAR(255) NOT NULL COMMENT '原因',
    detail TEXT DEFAULT NULL COMMENT '补充说明',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING/PROCESSING/RESOLVED/REJECTED',
    handled_by BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
    handled_at DATETIME DEFAULT NULL COMMENT '处理时间',
    handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_reports_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_handler FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT ck_reports_type CHECK (report_type IN ('REPORT', 'FEEDBACK')),
    CONSTRAINT ck_reports_status CHECK (status IN ('PENDING', 'PROCESSING', 'RESOLVED', 'REJECTED')),
    CONSTRAINT ck_reports_target_type CHECK (target_type IS NULL OR target_type IN ('ITEM', 'CLAIM', 'ANNOUNCEMENT', 'USER')),
    KEY idx_reports_target (target_type, target_id),
    KEY idx_reports_status_created (status, create_time),
    KEY idx_reports_reporter_created (reporter_id, create_time),
    KEY idx_reports_handler_created (handled_by, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报反馈表';

-- =====================================
-- 6. 系统日志
-- =====================================

CREATE TABLE operation_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
    operator_id BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID，系统操作可为空',
    target_type VARCHAR(32) NOT NULL COMMENT '对象类型',
    target_id BIGINT UNSIGNED DEFAULT NULL COMMENT '对象ID',
    action VARCHAR(64) NOT NULL COMMENT '操作动作',
    from_status VARCHAR(32) DEFAULT NULL COMMENT '原状态',
    to_status VARCHAR(32) DEFAULT NULL COMMENT '目标状态',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    request_ip VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_operation_logs_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL,
    KEY idx_operation_logs_operator_created (operator_id, create_time),
    KEY idx_operation_logs_target_created (target_type, target_id, create_time),
    KEY idx_operation_logs_action_created (action, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志';

CREATE TABLE contact_unlock_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '联系方式解锁日志ID',
    item_id BIGINT UNSIGNED NOT NULL COMMENT '物品ID',
    viewer_id BIGINT UNSIGNED NOT NULL COMMENT '查看人ID',
    source VARCHAR(32) NOT NULL COMMENT '来源：DETAIL_PAGE/LIST_PAGE/CLAIM_PAGE',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_unlock_logs_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_contact_unlock_logs_viewer FOREIGN KEY (viewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_contact_unlock_source CHECK (source IN ('DETAIL_PAGE', 'LIST_PAGE', 'CLAIM_PAGE')),
    KEY idx_contact_unlock_logs_item_viewer (item_id, viewer_id),
    KEY idx_contact_unlock_logs_created (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联系方式解锁日志';

-- =====================================
-- 7. 后台统计视图
-- =====================================

CREATE OR REPLACE VIEW v_admin_item_stats AS
SELECT
    scene,
    status,
    COUNT(*) AS item_count
FROM items
WHERE is_delete = 0
GROUP BY scene, status;

CREATE OR REPLACE VIEW v_admin_claim_stats AS
SELECT
    status,
    COUNT(*) AS claim_count
FROM claims
WHERE is_delete = 0
GROUP BY status;

CREATE OR REPLACE VIEW v_user_publish_stats AS
SELECT
    u.id AS user_id,
    u.student_no,
    u.name,
    COUNT(i.id) AS publish_count
FROM users u
LEFT JOIN items i ON i.owner_id = u.id AND i.is_delete = 0
WHERE u.is_delete = 0
GROUP BY u.id, u.student_no, u.name;
