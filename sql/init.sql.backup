-- =====================================
-- 校园失物招领系统数据库设计 SQL
-- MySQL 8.x
-- =====================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================
-- 1. 用户与权限管理
-- =====================================

DROP TABLE IF EXISTS operation_logs;
DROP TABLE IF EXISTS contact_unlock_logs;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS claim_audits;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS claim_drafts;
DROP TABLE IF EXISTS item_audits;
DROP TABLE IF EXISTS item_attachments;
DROP TABLE IF EXISTS item_contacts;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- 用户表
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_no VARCHAR(32) NOT NULL COMMENT '学号，唯一身份标识',
    name VARCHAR(64) NOT NULL COMMENT '姓名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    major VARCHAR(128) DEFAULT NULL COMMENT '专业',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_users_student_no UNIQUE (student_no),
    KEY idx_users_status (status),
    KEY idx_users_phone (phone),
    KEY idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(32) NOT NULL COMMENT '角色编码：ADMIN/USER',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统内置角色',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_roles_role_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE permissions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    perm_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型',
    resource_path VARCHAR(128) NOT NULL COMMENT '资源路径',
    action VARCHAR(32) NOT NULL COMMENT '操作类型',
    description VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_permissions_perm_code UNIQUE (perm_code),
    KEY idx_permissions_resource (resource_type, resource_path),
    KEY idx_permissions_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE user_roles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    KEY idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE role_permissions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT uk_role_permissions_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    KEY idx_role_permissions_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 分类表：
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_id VARCHAR(32) NOT NULL COMMENT '分类业务编号',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_categories_biz_id UNIQUE (biz_id),
    CONSTRAINT uk_categories_name UNIQUE (name),
    KEY idx_categories_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 物品主表：统一存储失物与招领信息
CREATE TABLE items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_id VARCHAR(32) NOT NULL COMMENT '业务编号',
    scene VARCHAR(16) NOT NULL COMMENT '场景：lost/found',
    stage VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT '阶段：draft/active/closed',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态：PENDING_REVIEW/PUBLISHED/CLAIMED/FOUND_BACK/REJECTED/OFFLINE',
    audit_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
    owner_id BIGINT NOT NULL COMMENT '发布人ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category VARCHAR(64) DEFAULT NULL COMMENT '分类',
    zone VARCHAR(64) DEFAULT NULL COMMENT '区域',
    location VARCHAR(255) NOT NULL COMMENT '地点',
    time_label VARCHAR(64) NOT NULL COMMENT '时间描述',
    verify_method VARCHAR(255) DEFAULT NULL COMMENT '认领验证方式',
    description TEXT DEFAULT NULL COMMENT '物品描述',
    value_tag VARCHAR(16) DEFAULT NULL COMMENT '价值标签',
    contact_visibility VARCHAR(16) NOT NULL DEFAULT 'UNMASKED' COMMENT '联系方式可见性：MASKED/UNMASKED',
    deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '删除时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_items_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    KEY idx_items_owner_id (owner_id),
    KEY idx_items_category (category),
    KEY idx_items_deleted_at (deleted_at),
    KEY idx_items_scene_status (scene, status),
    KEY idx_items_scene_stage_audit (scene, stage, audit_status),
    KEY idx_items_public_query (deleted_at, audit_status, stage, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品主表';

-- 物品联系方式表
CREATE TABLE item_contacts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    contact_type VARCHAR(32) NOT NULL COMMENT '联系方式类型',
    contact_value VARCHAR(255) NOT NULL COMMENT '联系方式内容',
    masked_value VARCHAR(255) NOT NULL COMMENT '脱敏联系方式',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_item_contacts_item_id UNIQUE (item_id),
    CONSTRAINT fk_item_contacts_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品联系方式表';

-- 物品附件表
CREATE TABLE item_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    file_url VARCHAR(255) NOT NULL COMMENT '文件地址',
    file_type VARCHAR(32) NOT NULL COMMENT '文件类型',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_item_attachments_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    KEY idx_item_attachments_item_sort (item_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品附件表';

-- 物品审核表
CREATE TABLE item_audits (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    audit_status VARCHAR(16) NOT NULL COMMENT '审核状态',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_item_audits_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_audits_auditor FOREIGN KEY (auditor_id) REFERENCES users(id) ON DELETE SET NULL,
    KEY idx_item_audits_item_created (item_id, created_at),
    KEY idx_item_audits_auditor_created (auditor_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品审核表';
-- 认领草稿表
CREATE TABLE claim_drafts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    draft_token VARCHAR(64) NOT NULL COMMENT '草稿令牌',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    claimant_id BIGINT NOT NULL COMMENT '认领人ID',
    keyword_proof TEXT NOT NULL COMMENT '关键词证明',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/USED/EXPIRED',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    used_at TIMESTAMP NULL DEFAULT NULL COMMENT '使用时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT uk_claim_drafts_token UNIQUE (draft_token),
    CONSTRAINT fk_claim_drafts_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_drafts_claimant FOREIGN KEY (claimant_id) REFERENCES users(id) ON DELETE CASCADE,
    KEY idx_claim_drafts_item_claimant (item_id, claimant_id),
    KEY idx_claim_drafts_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认领草稿表';

-- 认领申请主表
CREATE TABLE claims (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_id VARCHAR(32) NOT NULL COMMENT '业务编号',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    claimant_id BIGINT NOT NULL COMMENT '认领人ID',
    draft_id BIGINT DEFAULT NULL COMMENT '草稿ID',
    formal_proof TEXT NOT NULL COMMENT '正式证明材料',
    contact VARCHAR(255) NOT NULL COMMENT '联系信息',
    status VARCHAR(16) NOT NULL DEFAULT 'REVIEW_PENDING' COMMENT '状态：REVIEW_PENDING/APPROVED/REJECTED/EXPIRED/CANCELLED',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_claims_biz_id UNIQUE (biz_id),
    CONSTRAINT uk_claims_draft_id UNIQUE (draft_id),
    CONSTRAINT fk_claims_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_draft FOREIGN KEY (draft_id) REFERENCES claim_drafts(id) ON DELETE SET NULL,
    KEY idx_claims_item_status (item_id, status),
    KEY idx_claims_claimant_status (claimant_id, status),
    KEY idx_claims_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认领申请表';

-- 认领审核表
CREATE TABLE claim_audits (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL COMMENT '认领申请ID',
    audit_status VARCHAR(16) NOT NULL COMMENT '审核状态',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_claim_audits_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_audits_auditor FOREIGN KEY (auditor_id) REFERENCES users(id) ON DELETE SET NULL,
    KEY idx_claim_audits_claim_created (claim_id, created_at),
    KEY idx_claim_audits_auditor_created (auditor_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认领审核表';

-- 举报反馈表
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_id VARCHAR(32) NOT NULL COMMENT '举报业务编号',
    reporter_id BIGINT NOT NULL COMMENT '举报人 ID',
    target_type VARCHAR(32) NOT NULL COMMENT '举报对象类型：ITEM/CLAIM/ANNOUNCEMENT/USER',
    target_id BIGINT NOT NULL COMMENT '举报对象 ID',
    reason VARCHAR(255) NOT NULL COMMENT '举报原因',
    detail TEXT COMMENT '补充说明',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING/PROCESSING/RESOLVED/REJECTED',
    handled_by BIGINT NULL COMMENT '处理人 ID',
    handled_at DATETIME NULL COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_reports_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_handler FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL,
    KEY idx_reports_target (target_type, target_id),
    KEY idx_reports_status_created (status, created_at),
    KEY idx_reports_reporter_created (reporter_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- =====================================
-- 4. 公告与举报反馈
-- =====================================

-- 公告表
CREATE TABLE announcements (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    biz_id VARCHAR(32) NOT NULL COMMENT '公告业务编号',
    title VARCHAR(120) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态：PUBLISHED/OFFLINE',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    published_at TIMESTAMP NULL DEFAULT NULL COMMENT '发布时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_announcements_biz_id UNIQUE (biz_id),
    CONSTRAINT fk_announcements_publisher FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE RESTRICT,
    KEY idx_announcements_status_published (status, published_at),
    KEY idx_announcements_publisher_created (publisher_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 系统操作日志
CREATE TABLE operation_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    target_type VARCHAR(32) NOT NULL COMMENT '对象类型',
    target_id BIGINT NOT NULL COMMENT '对象ID',
    action VARCHAR(64) NOT NULL COMMENT '操作动作',
    from_status VARCHAR(16) DEFAULT NULL COMMENT '原状态',
    to_status VARCHAR(16) DEFAULT NULL COMMENT '目标状态',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_operation_logs_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE CASCADE,
    KEY idx_operation_logs_operator_created (operator_id, created_at),
    KEY idx_operation_logs_target_created (target_type, target_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志';
