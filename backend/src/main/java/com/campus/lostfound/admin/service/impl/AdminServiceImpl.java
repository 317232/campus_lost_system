package com.campus.lostfound.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.common.service.OperationLogService;
import com.campus.lostfound.domain.entity.ItemAudit;
import com.campus.lostfound.mapper.ItemAuditMapper;
import com.campus.lostfound.admin.service.AdminService;
import com.campus.lostfound.websocket.service.NotificationService;
import com.campus.lostfound.domain.entity.Category;
import com.campus.lostfound.domain.entity.Claim;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.Notice;
import com.campus.lostfound.domain.entity.Report;
import com.campus.lostfound.mapper.CategoryMapper;
import com.campus.lostfound.mapper.ClaimMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.NoticeMapper;
import com.campus.lostfound.mapper.ReportMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.security.SecurityUserUtils;
import com.campus.lostfound.common.api.PageResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ClaimMapper claimMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ItemAuditMapper itemAuditMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SecurityUserUtils securityUserUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private NotificationService notificationService;

    @Override
    public PageResponse<Map<String, Object>> listPendingItems(Integer page, Integer pageSize, String scene) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Item> pageObj =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        QueryWrapper<Item> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_REVIEW");
        if (scene != null && !scene.isEmpty()) {
            wrapper.eq("scene", scene);
        }
        wrapper.orderByDesc("created_at");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Item> result =
            itemMapper.selectPage(pageObj, wrapper);

        // 批量查询所有相关用户信息，避免 N+1 查询问题
        List<Long> ownerIds = result.getRecords().stream()
            .map(Item::getOwnerId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, User> userMap = ownerIds.isEmpty() ? new HashMap<>() :
            userMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Map<String, Object>> records = result.getRecords().stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("bizId", item.getBizId());
            m.put("scene", item.getScene());
            m.put("title", item.getTitle());
            m.put("itemName", item.getItemName());
            m.put("category", item.getCategory());
            m.put("location", item.getLocation());
            m.put("timeLabel", item.getTimeLabel());
            m.put("description", item.getDescription());
            m.put("status", item.getStatus());
            m.put("ownerId", item.getOwnerId());
            m.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
            // 使用预加载的用户信息
            User owner = userMap.get(item.getOwnerId());
            if (owner != null) {
                m.put("ownerName", owner.getName());
                m.put("ownerStudentNo", owner.getStudentNo());
            }
            return m;
        }).collect(Collectors.toList());

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    public PageResponse<Map<String, Object>> listPendingClaims(Integer page, Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Claim> pageObj =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        QueryWrapper<Claim> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "REVIEW_PENDING");
        wrapper.orderByDesc("created_at");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Claim> result =
            claimMapper.selectPage(pageObj, wrapper);

        // 批量查询所有相关物品和用户信息，避免 N+1 查询问题
        List<Long> itemIds = result.getRecords().stream()
            .map(Claim::getItemId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        List<Long> claimantIds = result.getRecords().stream()
            .map(Claim::getClaimantId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Item> itemMap = itemIds.isEmpty() ? new HashMap<>() :
            itemMapper.selectBatchIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, i -> i));
        Map<Long, User> userMap = claimantIds.isEmpty() ? new HashMap<>() :
            userMapper.selectBatchIds(claimantIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Map<String, Object>> records = result.getRecords().stream().map(claim -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", claim.getId());
            m.put("bizId", claim.getBizId());
            m.put("itemId", claim.getItemId());
            m.put("claimantId", claim.getClaimantId());
            m.put("formalProof", claim.getFormalProof());
            m.put("contact", claim.getContact());
            m.put("status", claim.getStatus());
            m.put("createdAt", claim.getCreatedAt() != null ? claim.getCreatedAt().toString() : null);
            // 使用预加载的物品信息
            Item item = itemMap.get(claim.getItemId());
            if (item != null) {
                m.put("itemTitle", item.getTitle());
                m.put("itemName", item.getItemName());
            }
            // 使用预加载的用户信息
            User claimant = userMap.get(claim.getClaimantId());
            if (claimant != null) {
                m.put("claimantName", claimant.getName());
                m.put("claimantStudentNo", claimant.getStudentNo());
            }
            return m;
        }).collect(Collectors.toList());

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditItem(AdminDTO.AuditItemReq request) {
        Long count = itemMapper.selectCount(new QueryWrapper<Item>().eq("id", request.getItemId()));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("待审核物品不存在");
        }

        String audit = normalizeAuditStatus(request.getAuditStatus());
        String targetStatus = "APPROVED".equals(audit) ? "PUBLISHED" : "REJECTED";
        
        // 更新物品状态（状态由 status 字段统一管理，不再使用 audit_status）
        UpdateWrapper<Item> updateWrapper = new UpdateWrapper<Item>()
            .eq("id", request.getItemId())
            .set("status", targetStatus);

        itemMapper.update(null, updateWrapper);
        
        // 添加审核记录到 item_audits 表（只记录审核操作历史）
        ItemAudit itemAudit = new ItemAudit();
        itemAudit.setItemId(request.getItemId());
        itemAudit.setAuditAction(audit);
        itemAudit.setAuditRemark(StringUtils.hasText(request.getAuditRemark()) ? request.getAuditRemark().trim() : null);
        
        // 获取当前审核员ID
        Long auditorId = securityUserUtils.getCurrentUserId();
        if (auditorId != null) {
            itemAudit.setAuditorId(auditorId);
        }
        
        itemAuditMapper.insert(itemAudit);

        // 发送 WebSocket 通知给物品发布者
        Item item = itemMapper.selectById(request.getItemId());
        if (item != null && item.getOwnerId() != null) {
            notificationService.notifyItemAuditResult(
                request.getItemId(),
                item.getOwnerId(),
                "APPROVED".equals(audit),
                request.getAuditRemark()
            );
        }

        // 记录操作日志
        operationLogService.log("ITEM", request.getItemId(), audit,
            "PENDING_REVIEW", targetStatus, request.getAuditRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditClaim(AdminDTO.AuditClaimReq request) {
        List<Map<String, Object>> rows = claimMapper.selectMaps(new QueryWrapper<Claim>()
            .select("id", "item_id")
            .eq("id", request.getClaimId())
            .last("LIMIT 1"));
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("待审核认领申请不存在");
        }

        String audit = normalizeAuditStatus(request.getAuditStatus());
        claimMapper.update(null, new UpdateWrapper<Claim>()
            .eq("id", request.getClaimId())
            .set("status", "APPROVED".equals(audit) ? "APPROVED" : "REJECTED")
            .set("audit_remark", request.getAuditRemark()));

        if ("APPROVED".equals(audit)) {
            Object itemIdObj = rows.get(0).get("item_id");
            if (itemIdObj != null) {
                itemMapper.update(null, new UpdateWrapper<Item>()
                    .eq("id", itemIdObj)
                    .set("status", "CLAIMED")
                    .set("stage", "closed"));
                // 记录操作日志
                operationLogService.log("ITEM", castLong(itemIdObj), "CLAIM_APPROVED",
                    "REVIEW_PENDING", "CLAIMED", request.getAuditRemark());
            }
        }
        // 记录认领审核日志
        operationLogService.log("CLAIM", request.getClaimId(), audit,
            "REVIEW_PENDING", "APPROVED".equals(audit) ? "APPROVED" : "REJECTED", request.getAuditRemark());

        // 发送 WebSocket 通知给申请人
        Claim claim = claimMapper.selectById(request.getClaimId());
        if (claim != null && claim.getClaimantId() != null) {
            notificationService.notifyClaimResult(
                request.getClaimId(),
                claim.getClaimantId(),
                "APPROVED".equals(audit),
                request.getAuditRemark()
            );
        }
    }

    @Override
    public Long createAnnouncement(AdminDTO.AnnouncementCreateReq request) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        boolean published = request.getPublished() != null && request.getPublished();
        notice.setPublished(published);
        notice.setPublishedAt(published ? LocalDateTime.now() : null);
        noticeMapper.insert(notice);

        List<Map<String, Object>> rows = noticeMapper.selectMaps(new QueryWrapper<Notice>()
            .select("id")
            .orderByDesc("id")
            .last("LIMIT 1"));
        return rows.isEmpty() ? null : castLong(rows.get(0).get("id"));
    }

    @Override
    public boolean updateAnnouncement(AdminDTO.AnnouncementUpdateReq request) {
        Notice notice = noticeMapper.selectById(request.getId());
        if (notice == null) {
            return false;
        }
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        if (request.getPublished() != null) {
            notice.setPublished(request.getPublished());
            notice.setPublishedAt(request.getPublished() ? LocalDateTime.now() : null);
        }
        return noticeMapper.updateById(notice) > 0;
    }

    @Override
    public boolean deleteAnnouncement(Long announcementId) {
        return noticeMapper.deleteById(announcementId) > 0;
    }

    @Override
    public Long createCategory(AdminDTO.CategoryUpsertReq request) {
        Category category = new Category();
        writeField(category, "bizId", generateBizId("CAT"));
        writeField(category, "name", request.getName().trim());
        writeField(category, "sortOrder", request.getSortOrder() == null ? 0 : request.getSortOrder());
        writeField(category, "status", StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "ENABLED");
        categoryMapper.insert(category);

        List<Map<String, Object>> rows = categoryMapper.selectMaps(new QueryWrapper<Category>()
            .select("id")
            .orderByDesc("id")
            .last("LIMIT 1"));
        return rows.isEmpty() ? null : castLong(rows.get(0).get("id"));
    }

    @Override
    public boolean updateCategory(AdminDTO.CategoryUpsertReq request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        Long count = categoryMapper.selectCount(new QueryWrapper<Category>().eq("id", request.getId()));
        if (count == null || count == 0) {
            return false;
        }

        UpdateWrapper<Category> updateWrapper = new UpdateWrapper<Category>()
            .eq("id", request.getId())
            .set("name", request.getName().trim());

        if (request.getSortOrder() != null) {
            updateWrapper.set("sort_order", request.getSortOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            updateWrapper.set("status", request.getStatus().trim());
        }
        return categoryMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public AdminDTO.DashboardResp dashboard() {
        long lostCount = safeCount(itemMapper.selectCount(new QueryWrapper<Item>().eq("scene", "lost")));
        long foundCount = safeCount(itemMapper.selectCount(new QueryWrapper<Item>().eq("scene", "found")));
        long totalClaims = safeCount(claimMapper.selectCount(new QueryWrapper<Claim>()));
        long approvedClaims = safeCount(claimMapper.selectCount(new QueryWrapper<Claim>().eq("status", "APPROVED")));

        double successRate = totalClaims == 0 ? 0D : (approvedClaims * 100D / totalClaims);
        return new AdminDTO.DashboardResp(lostCount, foundCount, approvedClaims, totalClaims, successRate);
    }

    @Override
    public AdminDTO.StatisticsTrendResp getStatisticsTrend(String startDate, String endDate, String type) {
        // Generate date range based on type
        List<String> dateRange;
        if ("monthly".equals(type)) {
            dateRange = generateMonthlyRange(startDate, endDate);
        } else {
            dateRange = generateDailyRange(startDate, endDate);
        }

        List<AdminDTO.StatisticsTrendResp.TrendDataPoint> data = dateRange.stream().map(date -> {
            long lostCount = safeCount(itemMapper.selectCount(new QueryWrapper<Item>()
                .eq("scene", "lost")
                .like("created_at", date)));
            long foundCount = safeCount(itemMapper.selectCount(new QueryWrapper<Item>()
                .eq("scene", "found")
                .like("created_at", date)));
            long claimCount = safeCount(claimMapper.selectCount(new QueryWrapper<Claim>()
                .like("created_at", date)));
            long approvedCount = safeCount(claimMapper.selectCount(new QueryWrapper<Claim>()
                .eq("status", "APPROVED")
                .like("created_at", date)));
            return new AdminDTO.StatisticsTrendResp.TrendDataPoint(date, lostCount, foundCount, claimCount, approvedCount);
        }).collect(Collectors.toList());

        AdminDTO.StatisticsTrendResp resp = new AdminDTO.StatisticsTrendResp();
        resp.setType(type);
        resp.setData(data);
        return resp;
    }

    private List<String> generateDailyRange(String startDate, String endDate) {
        // Default to last 7 days if not specified
        List<String> range = new java.util.ArrayList<>();
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : java.time.LocalDate.now();
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : end.minusDays(6);
        java.time.LocalDate current = start;
        while (!current.isAfter(end)) {
            range.add(current.toString());
            current = current.plusDays(1);
        }
        return range;
    }

    private List<String> generateMonthlyRange(String startDate, String endDate) {
        List<String> range = new java.util.ArrayList<>();
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : java.time.LocalDate.now();
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : end.minusMonths(5).withDayOfMonth(1);
        java.time.LocalDate current = start.withDayOfMonth(1);
        while (!current.isAfter(end.withDayOfMonth(1))) {
            range.add(current.toString().substring(0, 7)); // YYYY-MM
            current = current.plusMonths(1);
        }
        return range;
    }

    @Override
    public boolean handleReport(AdminDTO.HandleReportReq request) {
        Long count = reportMapper.selectCount(new QueryWrapper<Report>().eq("id", request.getReportId()));
        if (count == null || count == 0) {
            return false;
        }

        UpdateWrapper<Report> updateWrapper = new UpdateWrapper<Report>()
            .eq("id", request.getReportId())
            .set("status", request.getStatus().trim().toUpperCase(Locale.ROOT))
            .set("handle_remark", request.getHandleRemark())
            .set("handled_at", LocalDateTime.now());

        Long handlerId = securityUserUtils.getCurrentUserId();
        if (handlerId != null) {
            updateWrapper.set("handled_by", handlerId);
        }
        return reportMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public PageResponse<AdminDTO.NoticeResp> listNotices(Integer page, Integer pageSize, String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notice> pageObj =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        wrapper.orderByDesc("id");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notice> result =
            noticeMapper.selectPage(pageObj, wrapper);

        List<AdminDTO.NoticeResp> records = result.getRecords().stream().map(n -> {
            AdminDTO.NoticeResp r = new AdminDTO.NoticeResp();
            r.setId(castLong(n.getId()));
            r.setTitle(n.getTitle());
            r.setContent(n.getContent());
            r.setPublished(n.getPublished());
            r.setPublishedAt(n.getPublishedAt() != null ? n.getPublishedAt().toString() : null);
            r.setCreateTime(n.getCreateTime() != null ? n.getCreateTime().toString() : null);
            return r;
        }).collect(Collectors.toList());

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    public List<AdminDTO.CategoryResp> listCategories() {
        return categoryMapper.selectList(new QueryWrapper<Category>().orderByAsc("sort_order").orderByDesc("id"))
            .stream().map(c -> {
                AdminDTO.CategoryResp r = new AdminDTO.CategoryResp();
                r.setId(castLong(c.getId()));
                r.setBizId(c.getBizId());
                r.setName(c.getName());
                r.setSortOrder(c.getSortOrder());
                r.setStatus(c.getStatus());
                r.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().toString() : null);
                return r;
            }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteCategory(Long categoryId) {
        return categoryMapper.deleteById(categoryId) > 0;
    }

    @Override
    public PageResponse<AdminDTO.ReportResp> listReports(Integer page, Integer pageSize, String status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Report> pageObj =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Report> result =
            reportMapper.selectPage(pageObj, wrapper);

        List<AdminDTO.ReportResp> records = result.getRecords().stream().map(rp -> {
            AdminDTO.ReportResp r = new AdminDTO.ReportResp();
            r.setId(rp.getId());
            r.setBizId(rp.getBizId());
            r.setReporterId(rp.getReporterId());
            r.setTargetType(rp.getTargetType());
            r.setTargetId(rp.getTargetId());
            r.setReason(rp.getReason());
            r.setDetail(rp.getDetail());
            r.setStatus(rp.getStatus());
            r.setHandleRemark(rp.getHandleRemark());
            r.setHandledAt(rp.getHandledAt() != null ? rp.getHandledAt().toString() : null);
            r.setCreateTime(rp.getCreateTime() != null ? rp.getCreateTime().toString() : null);
            return r;
        }).collect(Collectors.toList());

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    public boolean deleteReport(Long reportId) {
        return reportMapper.deleteById(reportId) > 0;
    }

    @Override
    public PageResponse<AdminDTO.UserListResp> listUsers(Integer page, Integer pageSize, String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> pageObj =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("student_no", keyword).or().like("phone", keyword));
        }
        wrapper.orderByDesc("id");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result =
            userMapper.selectPage(pageObj, wrapper);

        List<AdminDTO.UserListResp> records = result.getRecords().stream().map(u -> {
            AdminDTO.UserListResp resp = new AdminDTO.UserListResp();
            resp.setId(u.getId());
            resp.setStudentNo(u.getStudentNo());
            resp.setName(u.getName());
            resp.setPhone(u.getPhone());
            resp.setEmail(u.getEmail());
            resp.setMajor(u.getMajor());
            resp.setAvatarUrl(u.getAvatarUrl());
            resp.setStatus(u.getStatus());
            return resp;
        }).collect(Collectors.toList());

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new IllegalArgumentException("用户状态仅支持 ACTIVE 或 INACTIVE");
        }
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("id", userId));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.update(null, new UpdateWrapper<User>()
            .eq("id", userId)
            .set("status", normalized));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(AdminDTO.CreateUserReq request) {
        Long exists = userMapper.selectCount(new QueryWrapper<User>().eq("student_no", request.getStudentNo().trim()));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("学号已存在：" + request.getStudentNo());
        }

        User user = new User();
        user.setStudentNo(request.getStudentNo().trim());
        user.setName(request.getName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (org.springframework.util.StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone().trim());
        }
        if (org.springframework.util.StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().trim());
        }
        if (org.springframework.util.StringUtils.hasText(request.getMajor())) {
            user.setMajor(request.getMajor().trim());
        }
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long userId, AdminDTO.UpdateUserReq request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        UpdateWrapper<User> uw = new UpdateWrapper<User>().eq("id", userId);
        if (org.springframework.util.StringUtils.hasText(request.getName())) {
            uw.set("name", request.getName().trim());
        }
        if (org.springframework.util.StringUtils.hasText(request.getPhone())) {
            uw.set("phone", request.getPhone().trim());
        }
        if (org.springframework.util.StringUtils.hasText(request.getEmail())) {
            uw.set("email", request.getEmail().trim());
        }
        if (org.springframework.util.StringUtils.hasText(request.getMajor())) {
            uw.set("major", request.getMajor().trim());
        }
        return userMapper.update(null, uw) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("id", userId));
        if (count == null || count == 0) {
            return false;
        }
        return userMapper.deleteById(userId) > 0;
    }

    private String normalizeAuditStatus(String input) {
        String value = input.trim().toUpperCase(Locale.ROOT);
        if (!"APPROVED".equals(value) && !"REJECTED".equals(value)) {
            throw new IllegalArgumentException("审核状态仅支持 APPROVED 或 REJECTED");
        }
        return value;
    }

    private String generateBizId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    private long safeCount(Long count) {
        return count == null ? 0L : count;
    }

    private Long castLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private void writeField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("字段写入失败: " + fieldName, e);
        }
    }
}