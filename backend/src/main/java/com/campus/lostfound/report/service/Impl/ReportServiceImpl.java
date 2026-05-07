package com.campus.lostfound.report.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.domain.entity.Report;
import com.campus.lostfound.report.dto.ReportDTO;
import com.campus.lostfound.report.service.ReportService;
import com.campus.lostfound.mapper.ReportMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private SecurityUserUtils securityUserUtils;

    @Override
    public Page<ReportDTO.ReportResp> getReportsPage(Integer pageNum, Integer pageSize, String status, String targetType) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Report::getStatus, status);
        }

        if (StringUtils.hasText(targetType)) {
            queryWrapper.eq(Report::getTargetType, targetType);
        }

        queryWrapper.orderByDesc(Report::getCreateTime);

        Page<Report> reportPage = reportMapper.selectPage(page, queryWrapper);
        Page<ReportDTO.ReportResp> dtoPage = new Page<>(
            reportPage.getCurrent(),
            reportPage.getSize(),
            reportPage.getTotal()
        );

        List<ReportDTO.ReportResp> dtoList = reportPage.getRecords()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public ReportDTO.ReportResp getReportById(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return null;
        }
        return convertToDto(report);
    }

    @Override
    public void createReport(ReportDTO.CreateReportReq request) {
        Report report = new Report();
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setDetail(request.getDetail());
        report.setStatus("PENDING");
        reportMapper.insert(report);
    }

    @Override
    public boolean updateReport(ReportDTO.UpdateReportReq request) {
        if (request.getId() == null) {
            return false;
        }

        Report report = reportMapper.selectById(request.getId());
        if (report == null) {
            return false;
        }

        report.setStatus(request.getStatus());
        report.setHandleRemark(request.getHandleRemark());

        if ("RESOLVED".equals(request.getStatus()) || "REJECTED".equals(request.getStatus())) {
            report.setHandledAt(LocalDateTime.now());
        }

        return reportMapper.updateById(report) > 0;
    }

    @Override
    public boolean deleteReport(Long id) {
        return reportMapper.deleteById(id) > 0;
    }

    @Override
    public PageResponse<ReportDTO.ReportResp> getMyReports(Integer pageNum, Integer pageSize, String status) {
        Long userId = securityUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录后再查看举报记录");
        }

        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Report::getReporterId, userId);

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Report::getStatus, status);
        }

        queryWrapper.orderByDesc(Report::getCreateTime);

        Page<Report> reportPage = reportMapper.selectPage(page, queryWrapper);

        List<ReportDTO.ReportResp> dtoList = reportPage.getRecords()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return new PageResponse<>(dtoList, reportPage.getTotal(), pageNum, pageSize);
    }

    private ReportDTO.ReportResp convertToDto(Report report) {
        ReportDTO.ReportResp dto = new ReportDTO.ReportResp();
        dto.setId(report.getId());
        dto.setBizId(report.getBizId());
        dto.setReporterId(report.getReporterId());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReason(report.getReason());
        dto.setDetail(report.getDetail());
        dto.setStatus(report.getStatus());
        dto.setHandledBy(report.getHandledBy());
        dto.setHandledAt(report.getHandledAt());
        dto.setHandleRemark(report.getHandleRemark());
        dto.setCreateTime(report.getCreateTime());
        return dto;
    }
}