package com.campus.lostfound.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.lostfound.admin.dto.AdminDTO;
import com.campus.lostfound.admin.service.AdminService;
import com.campus.lostfound.domain.entity.Category;
import com.campus.lostfound.domain.entity.Report;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.CategoryMapper;
import com.campus.lostfound.mapper.ReportMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import com.campus.lostfound.common.api.PageResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {

    private final CategoryMapper categoryMapper;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final SecurityUserUtils securityUserUtils;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(CategoryMapper categoryMapper,
                           ReportMapper reportMapper,
                           UserMapper userMapper,
                           SecurityUserUtils securityUserUtils,
                           PasswordEncoder passwordEncoder) {
        this.categoryMapper = categoryMapper;
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.securityUserUtils = securityUserUtils;
        this.passwordEncoder = passwordEncoder;
    }

    // ── 分类 CRUD ──────────────────────────────────────────────

    @Override
    public List<AdminDTO.CategoryResp> listCategories() {
        return categoryMapper.selectList(
                new QueryWrapper<Category>().orderByAsc("sort_order").orderByDesc("id"))
            .stream().map(c -> {
                AdminDTO.CategoryResp r = new AdminDTO.CategoryResp();
                r.setId(castLong(c.getId()));
                r.setBizId(c.getBizId());
                r.setName(c.getName());
                r.setSortOrder(c.getSortOrder());
                r.setStatus(c.getStatus());
                r.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().toString() : null);
                return r;
            }).toList();
    }

    @Override
    @Transactional
    public Long createCategory(AdminDTO.CategoryUpsertReq request) {
        Category category = new Category();
        category.setBizId(generateBizId("CAT"));
        category.setName(request.getName().trim());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "ENABLED");
        categoryMapper.insert(category);
        return category.getId();
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
        UpdateWrapper<Category> uw = new UpdateWrapper<Category>()
            .eq("id", request.getId())
            .set("name", request.getName().trim());
        if (request.getSortOrder() != null) {
            uw.set("sort_order", request.getSortOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            uw.set("status", request.getStatus().trim());
        }
        return categoryMapper.update(null, uw) > 0;
    }

    @Override
    public boolean deleteCategory(Long categoryId) {
        return categoryMapper.deleteById(categoryId) > 0;
    }

    // ── 举报 CRUD ──────────────────────────────────────────────

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
        }).toList();

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    public boolean handleReport(AdminDTO.HandleReportReq request) {
        Long count = reportMapper.selectCount(new QueryWrapper<Report>().eq("id", request.getReportId()));
        if (count == null || count == 0) {
            return false;
        }
        UpdateWrapper<Report> uw = new UpdateWrapper<Report>()
            .eq("id", request.getReportId())
            .set("status", request.getStatus().trim().toUpperCase(Locale.ROOT))
            .set("handle_remark", request.getHandleRemark())
            .set("handled_at", LocalDateTime.now());
        Long handlerId = securityUserUtils.getCurrentUserId();
        if (handlerId != null) {
            uw.set("handled_by", handlerId);
        }
        return reportMapper.update(null, uw) > 0;
    }

    @Override
    public boolean deleteReport(Long reportId) {
        return reportMapper.deleteById(reportId) > 0;
    }

    // ── 用户管理 ───────────────────────────────────────────────

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
        }).toList();

        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public Long createUser(AdminDTO.CreateUserReq request) {
        Long exists = userMapper.selectCount(
            new QueryWrapper<User>().eq("student_no", request.getStudentNo().trim()));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("学号已存在：" + request.getStudentNo());
        }
        User user = new User();
        user.setStudentNo(request.getStudentNo().trim());
        user.setName(request.getName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().trim());
        }
        if (StringUtils.hasText(request.getMajor())) {
            user.setMajor(request.getMajor().trim());
        }
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional
    public boolean updateUser(Long userId, AdminDTO.UpdateUserReq request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        UpdateWrapper<User> uw = new UpdateWrapper<User>().eq("id", userId);
        if (StringUtils.hasText(request.getName())) {
            uw.set("name", request.getName().trim());
        }
        if (StringUtils.hasText(request.getPhone())) {
            uw.set("phone", request.getPhone().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            uw.set("email", request.getEmail().trim());
        }
        if (StringUtils.hasText(request.getMajor())) {
            uw.set("major", request.getMajor().trim());
        }
        return userMapper.update(null, uw) > 0;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("id", userId));
        if (count == null || count == 0) {
            return false;
        }
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"DISABLED".equals(normalized)) {
            throw new IllegalArgumentException("用户状态仅支持 ACTIVE 或 DISABLED");
        }
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("id", userId));
        if (count == null || count == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.update(null, new UpdateWrapper<User>()
            .eq("id", userId)
            .set("status", normalized));
    }

    // ── 工具方法 ───────────────────────────────────────────────

    private String generateBizId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    private Long castLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}
