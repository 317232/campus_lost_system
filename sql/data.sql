-- =====================================
-- 校园失物招领系统初始化数据脚本
-- 密码统一使用：123456（bcrypt $2a$10$ hash）
-- =====================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================
-- 1. 用户数据
-- 用户密码：123456（bcrypt $2a$10$...）
-- =====================================

INSERT INTO users (student_no, name, password_hash, phone, email, major, avatar_url, status) VALUES
('2021001', '张三', '$2a$10$pNxDDdMwPIQaxergscmY0egVP.CN6zzO1k3EMjnuRneJpQP.PKjuG', '13800001001', 'zhangsan@campus.edu', '计算机科学与技术', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', 'ACTIVE'),
('2021002', '李四', '$2a$10$pNxDDdMwPIQaxergscmY0egVP.CN6zzO1k3EMjnuRneJpQP.PKjuG', '13800001002', 'lisi@campus.edu', '软件工程', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', 'ACTIVE'),
('2021003', '王五', '$2a$10$pNxDDdMwPIQaxergscmY0egVP.CN6zzO1k3EMjnuRneJpQP.PKjuG', '13800001003', 'wangwu@campus.edu', '信息安全', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', 'ACTIVE'),
('2021004', '赵六', '$2a$10$pNxDDdMwPIQaxergscmY0egVP.CN6zzO1k3EMjnuRneJpQP.PKjuG', '13800001004', 'zhaoliu@campus.edu', '数据科学', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu', 'ACTIVE'),
('2024001', '管理员', '$2a$10$pNxDDdMwPIQaxergscmY0egVP.CN6zzO1k3EMjnuRneJpQP.PKjuG', '13900001000', 'admin@campus.edu', '系统管理', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'ACTIVE');

-- =====================================
-- 2. 角色数据
-- =====================================

INSERT INTO roles (role_code, role_name, description, is_system) VALUES
('ADMIN', '管理员', '系统管理员，拥有全部权限', TRUE),
('USER', '普通用户', '普通用户，可发布失物招领', TRUE);

-- =====================================
-- 3. 权限数据
-- =====================================

INSERT INTO permissions (perm_code, perm_name, resource_type, resource_path, action, description) VALUES
-- 用户权限
('user:profile:view', '查看个人资料', 'USER', '/api/users/me', 'GET', '查看当前用户资料'),
('user:profile:update', '更新个人资料', 'USER', '/api/users/me', 'PUT', '更新当前用户资料'),
('user:items:view', '查看用户物品', 'USER', '/api/users/*/items', 'GET', '查看用户发布的物品'),
-- 物品权限
('item:publish', '发布物品', 'ITEM', '/api/items', 'POST', '发布失物或招领'),
('item:list', '物品列表', 'ITEM', '/api/items', 'GET', '查看物品列表'),
('item:detail', '物品详情', 'ITEM', '/api/items/*', 'GET', '查看物品详情'),
('item:update', '更新物品', 'ITEM', '/api/items/*', 'PUT', '更新物品信息'),
('item:delete', '删除物品', 'ITEM', '/api/items/*', 'DELETE', '删除物品'),
-- 认领权限
('claim:apply', '申请认领', 'CLAIM', '/api/claims', 'POST', '提交认领申请'),
('claim:list', '认领列表', 'CLAIM', '/api/claims', 'GET', '查看认领列表'),
('claim:detail', '认领详情', 'CLAIM', '/api/claims/*', 'GET', '查看认领详情'),
('claim:cancel', '取消认领', 'CLAIM', '/api/claims/*/cancel', 'PUT', '取消认领申请'),
-- 公告权限
('notice:publish', '发布公告', 'NOTICE', '/api/notices', 'POST', '发布系统公告'),
('notice:list', '公告列表', 'NOTICE', '/api/notices', 'GET', '查看公告列表'),
('notice:update', '更新公告', 'NOTICE', '/api/notices/*', 'PUT', '更新公告'),
('notice:delete', '删除公告', 'NOTICE', '/api/notices/*', 'DELETE', '删除公告'),
-- 分类权限
('category:list', '分类列表', 'CATEGORY', '/api/categories', 'GET', '查看分类列表'),
('category:manage', '管理分类', 'CATEGORY', '/api/categories', 'POST', '管理分类'),
-- 举报权限
('report:create', '提交举报', 'REPORT', '/api/reports', 'POST', '提交举报'),
('report:list', '举报列表', 'REPORT', '/api/reports', 'GET', '查看举报列表（管理员）'),
-- 管理权限
('admin:statistics', '统计数据', 'ADMIN', '/api/admin/statistics', 'GET', '管理员查看统计'),
('admin:audit', '审核操作', 'ADMIN', '/api/admin/*/audit', 'POST', '管理员审核物品'),
('admin:user:list', '用户列表', 'ADMIN', '/api/admin/users', 'GET', '管理员查看用户列表');

-- =====================================
-- 4. 用户角色关联
-- =====================================

INSERT INTO user_roles (user_id, role_id) VALUES
(5, 1),  -- 管理员 -> 管理员角色
(1, 2),  -- 张三 -> 普通用户角色
(2, 2),  -- 李四 -> 普通用户角色
(3, 2),  -- 王五 -> 普通用户角色
(4, 2);  -- 赵六 -> 普通用户角色

-- =====================================
-- 5. 角色权限关联
-- =====================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.role_code = 'ADMIN' AND p.perm_code IN (
    'user:profile:view', 'user:profile:update', 'user:items:view',
    'item:publish', 'item:list', 'item:detail', 'item:update', 'item:delete',
    'claim:apply', 'claim:list', 'claim:detail', 'claim:cancel',
    'notice:publish', 'notice:list', 'notice:update', 'notice:delete',
    'category:list', 'category:manage',
    'report:create', 'report:list',
    'admin:statistics', 'admin:audit', 'admin:user:list'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.role_code = 'USER' AND p.perm_code IN (
    'user:profile:view', 'user:profile:update', 'user:items:view',
    'item:publish', 'item:list', 'item:detail', 'item:update', 'item:delete',
    'claim:apply', 'claim:list', 'claim:detail', 'claim:cancel',
    'notice:list',
    'category:list',
    'report:create'
);

-- =====================================
-- 6. 分类数据
-- =====================================

INSERT INTO categories (biz_id, name, sort_order, status) VALUES
('CAT001', '校园卡', 1, 'ENABLED'),
('CAT002', '身份证', 2, 'ENABLED'),
('CAT003', '钥匙', 3, 'ENABLED'),
('CAT004', '手机', 4, 'ENABLED'),
('CAT005', '电脑/平板', 5, 'ENABLED'),
('CAT006', '耳机/音响', 6, 'ENABLED'),
('CAT007', '书包/背包', 7, 'ENABLED'),
('CAT008', '雨伞', 8, 'ENABLED'),
('CAT009', '水杯', 9, 'ENABLED'),
('CAT010', '书籍/笔记', 10, 'ENABLED'),
('CAT011', '钱包/现金', 11, 'ENABLED'),
('CAT012', '其他', 12, 'ENABLED');

-- =====================================
-- 7. 公告数据
-- =====================================

INSERT INTO announcements (biz_id, title, content, status, publisher_id, published_at) VALUES
('NOTICE001', '新学期失物招领平台使用指南', '各位同学：新学期伊始，平台已完成功能升级，请仔细阅读以下使用说明：\n\n1. 发布信息：登录后点击"发布信息"按钮，选择失物或招领类型，填写详细信息后提交。\n2. 认领流程：看到匹配的物品可直接提交认领申请，填写联系方式和证明材料。\n3. 联系方式：平台保护双方隐私，联系方式在双方确认后可见。\n4. 文明用语：平台内请使用文明用语，共同维护良好的校园环境。\n\n如有疑问请联系管理员。', 'PUBLISHED', 5, NOW()),
('NOTICE002', '关于规范失物招领信息发布的通知', '为维护平台信息质量，现将信息发布规范通知如下：\n\n1. 信息描述需真实准确，不得虚构物品信息。\n2. 物品价值超过500元请到学校保卫处备案。\n3. 同一物品不得重复发布，一经发现将删除处理。\n4. 发布虚假信息者将被禁止使用平台功能。\n\n请各位同学共同遵守。', 'PUBLISHED', 5, NOW());

-- =====================================
-- 8. 物品数据（失物 + 招领）
-- =====================================

INSERT INTO items (biz_id, scene, stage, status, owner_id, title, item_name, category, zone, location, time_label, verify_method, description, value_tag, contact_visibility) VALUES
-- 失物
('ITEML001', 'lost', 'active', 'PUBLISHED', 1, '遗失校园卡一张', '校园卡', '校园卡', '东区', '图书馆三楼阅览室', '2024-03-15 上午', '学号+姓名核实', '卡上姓名为张三，卡号2021001，紫色卡套', 'LOW', 'UNMASKED'),
('ITEML002', 'lost', 'active', 'PUBLISHED', 2, '丢失黑色双肩包', '书包/背包', '书包/背包', '西区', '食堂至宿舍楼路上', '2024-03-14 中午', '包内物品照片核实', '黑色小米双肩包，内有笔记本电脑和课本', 'HIGH', 'UNMASKED'),
('ITEML003', 'lost', 'active', 'PUBLISHED', 3, '钥匙串丢失', '钥匙', '钥匙', '北区', '操场看台', '2024-03-13 下午', '钥匙形状/数量核实', '共5把钥匙，含电动车钥匙和宿舍钥匙', 'LOW', 'UNMASKED'),
('ITEML004', 'lost', 'closed', 'CLAIMED', 4, '丢失蓝牙耳机', '耳机/音响', '耳机/音响', '东区', '教学楼A座201', '2024-03-12 上午', '耳机序列号核实', '白色AirPods Pro，左耳有轻微划痕，充电盒背面刻有字母Z', 'MEDIUM', 'UNMASKED'),
('ITEML005', 'lost', 'active', 'PUBLISHED', 1, '专业课笔记遗失', '书籍/笔记', '书籍/笔记', '西区', '教学楼B座3楼走廊', '2024-03-16 下午', '笔记内容核实', '《计算机网络》课堂笔记，蓝色笔记本，约30页', 'LOW', 'UNMASKED'),

-- 招领
('ITEMF001', 'found', 'active', 'PUBLISHED', 2, '捡到校园卡一张', '校园卡', '校园卡', '东区', '图书馆门口保安亭', '2024-03-15 下午', '学号+姓名核实', '卡上姓名为李四，请失主带身份证到保安亭领取', 'LOW', 'MASKED'),
('ITEMF002', 'found', 'active', 'PUBLISHED', 3, '食堂门口捡到雨伞', '雨伞', '雨伞', '西区', '学生食堂一楼入口', '2024-03-14 晚上', '颜色/品牌描述核实', '黑色全自动雨伞，带有轻微损坏', 'LOW', 'MASKED'),
('ITEMF003', 'found', 'active', 'PUBLISHED', 4, '操场跑道捡到水杯', '水杯', '水杯', '北区', '田径场跑道旁', '2024-03-13 上午', '水杯特征/内含物核实', '白色保温杯，杯身贴有学院logo，内有半杯水', 'LOW', 'MASKED'),
('ITEMF004', 'found', 'active', 'PUBLISHED', 5, '图书馆研读室发现平板电脑', '电脑/平板', '电脑/平板', '东区', '图书馆五楼研读室C区', '2024-03-12 下午', '设备序列号+icloud账号核实', '银色iPad Air 5，机身有轻微磨损，贴有磨砂膜', 'HIGH', 'MASKED');

-- =====================================
-- 9. 物品联系方式
-- =====================================

INSERT INTO item_contacts (item_id, contact_type, contact_value, masked_value) VALUES
(1, 'PHONE', '13800001001', '138****1001'),
(2, 'PHONE', '13800001002', '138****1002'),
(3, 'PHONE', '13800001003', '138****1003'),
(4, 'PHONE', '13800001004', '138****1004'),
(5, 'PHONE', '13800001001', '138****1001'),
(6, 'PHONE', '13800001002', '138****1002'),
(7, 'PHONE', '13800001003', '138****1003'),
(8, 'PHONE', '13800001004', '138****1004'),
(9, 'PHONE', '13900001000', '139****1000');

-- =====================================
-- 10. 认领数据
-- =====================================

INSERT INTO claims (biz_id, item_id, claimant_id, draft_id, formal_proof, contact, status) VALUES
('CLAIM001', 4, 4, NULL, '这是我的耳机，序列号可查，充电盒背面确实刻有字母Z。', '手机：13800001004', 'APPROVED');

-- 认领草稿（用于生成认领链接）
INSERT INTO claim_drafts (draft_token, item_id, claimant_id, keyword_proof, status, expires_at) VALUES
('DRAFT_TOKEN_001', 4, 4, 'AirPods Pro, 刻字Z, 左耳划痕', 'USED', DATE_ADD(NOW(), INTERVAL 7 DAY));

SET FOREIGN_KEY_CHECKS = 1;
