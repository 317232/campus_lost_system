-- =====================================
-- 校园失物招领系统数据库删除脚本
-- 用于清除所有表数据（保留数据库）
-- 执行顺序：先删子表，再删主表
-- =====================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 系统日志
DROP TABLE IF EXISTS contact_unlock_logs;
DROP TABLE IF EXISTS operation_logs;

-- 举报反馈（依赖 users）
DROP TABLE IF EXISTS reports;

-- 公告（依赖 users）
DROP TABLE IF EXISTS announcements;

-- 认领审核（依赖 claims）
DROP TABLE IF EXISTS claim_audits;

-- 认领申请（依赖 items, users, claim_drafts）
DROP TABLE IF EXISTS claims;

-- 认领草稿（依赖 items, users）
DROP TABLE IF EXISTS claim_drafts;

-- 物品审核（依赖 items, users）
DROP TABLE IF EXISTS item_audits;

-- 物品附件（依赖 items）
DROP TABLE IF EXISTS item_attachments;

-- 物品联系方式（依赖 items）
DROP TABLE IF EXISTS item_contacts;

-- 物品主表（依赖 users）
DROP TABLE IF EXISTS items;

-- 角色权限（依赖 roles, permissions）
DROP TABLE IF EXISTS role_permissions;

-- 用户角色（依赖 users, roles）
DROP TABLE IF EXISTS user_roles;

-- 权限表
DROP TABLE IF EXISTS permissions;

-- 角色表
DROP TABLE IF EXISTS roles;

-- 用户表
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;
