package com.campus.lostfound.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.admin.service.AdminService;
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
import com.campus.lostfound.security.SecurityUserUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminServiceImpl implements AdminService {

    private final ItemMapper itemMapper;
    private final ClaimMapper claimMapper;
    private final NoticeMapper noticeMapper;
    private final CategoryMapper categoryMapper;
    private final ReportMapper reportMapper;
    private final SecurityUserUtils securityUserUtils;

    public AdminServiceImpl(ItemMapper itemMapper,
                            ClaimMapper claimMapper,
                            NoticeMapper noticeMapper,
                            CategoryMapper categoryMapper,
                            ReportMapper reportMapper,
                            SecurityUserUtils securityUserUtils) {
        this.itemMapper = itemMapper;
        this.claimMapper = claimMapper;
        this.noticeMapper = noticeMapper;
        this.categoryMapper = categoryMapper;
        this.reportMapper = reportMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public void auditItem(AdminDTO.AuditItemReq request) {
        Long count = itemMapper.selectCount(new QueryWrapper<Item>().eq("id", request.getItemId()));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("待审核物品不存在");
        }

        String audit = normalizeAuditStatus(request.getAuditStatus());
        UpdateWrapper<Item> updateWrapper = new UpdateWrapper<Item>()
            .eq("id", request.getItemId())
            .set("audit_status", audit)
            .set("status", "APPROVED".equals(audit) ? "PUBLISHED" : "REJECTED");

        if (StringUtils.hasText(request.getAuditRemark())) {
            updateWrapper.set("verify_method", request.getAuditRemark().trim());
        }
        itemMapper.update(null, updateWrapper);
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
            }
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
