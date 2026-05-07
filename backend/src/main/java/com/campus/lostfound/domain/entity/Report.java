package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reports")
public class Report extends BaseEntity {

    @TableField("biz_id")
    private String bizId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("report_type")
    private String reportType;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    private String reason;

    private String detail;

    private String status;

    @TableField("handled_by")
    private Long handledBy;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("handle_remark")
    private String handleRemark;
}
