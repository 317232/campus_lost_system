package com.campus.lostfound.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.report.dto.ReportDTO;

public interface ReportService {
    Page<ReportDTO.ReportResp> getReportsPage(Integer pageNum, Integer pageSize, String status, String targetType);

    ReportDTO.ReportResp getReportById(Long id);

    void createReport(ReportDTO.CreateReportReq request);

    boolean updateReport(ReportDTO.UpdateReportReq request);

    boolean deleteReport(Long id);

    /** 用户查看自己的举报记录 */
    PageResponse<ReportDTO.ReportResp> getMyReports(Integer pageNum, Integer pageSize, String status);
}