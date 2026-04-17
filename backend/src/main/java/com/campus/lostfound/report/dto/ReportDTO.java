package com.campus.lostfound.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportDTO {

    @Data
    public static class CreateReportReq {
        @NotNull(message = "举报对象类型不能为空")
        private String targetType;

        @NotNull(message = "举报对象ID不能为空")
        private Long targetId;

        @NotBlank(message = "举报原因不能为空")
        private String reason;

        private String detail;
    }

    @Data
    public static class UpdateReportReq {
        private Long id;

        @NotBlank(message = "处理状态不能为空")
        private String status;

        private String handleRemark;
    }

    @Data
    public static class ReportResp {
        private Long id;
        private String bizId;
        private Long reporterId;
        private String reporterName;
        private String targetType;
        private Long targetId;
        private String targetDescription;
        private String reason;
        private String detail;
        private String status;
        private Long handledBy;
        private String handlerName;
        private LocalDateTime handledAt;
        private String handleRemark;
        private LocalDateTime createTime;
    }

    @Data
    public static class ReportQueryReq {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String status;
        private String targetType;
        private Long targetId;
    }
}