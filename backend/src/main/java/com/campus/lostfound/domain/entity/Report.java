package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report")
public class Report extends BaseEntity {
@TableId(type = IdType.AUTO)
    private Long id;

    @TableField("biz_id")
    private String bizId; // 举报业务编号

    @TableField("reporter_id")
    private Long reporterId; // 举报人 ID

    @TableField("target_type")
    private String targetType; // 举报对象类型：ITEM/CLAIM/ANNOUNCEMENT/USER

    @TableField("target_id")
    private Long targetId; // 举报对象 ID

    private String reason; // 举报原因

    private String detail; // 补充说明

    private String status; // 处理状态：PENDING/PROCESSING/RESOLVED/REJECTED

    @TableField("handled_by")
    private Long handledBy; // 处理人 ID

    @TableField("handled_at")
    private LocalDateTime handledAt; // 处理时间

    @TableField("handle_remark")
    private String handleRemark; // 处理备注

}
