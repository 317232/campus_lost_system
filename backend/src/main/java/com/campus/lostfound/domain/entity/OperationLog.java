package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_logs")
public class OperationLog extends BaseEntity {
  @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("operator_id")
    private Long operatorId; // 操作人ID

    @TableField("target_type")
    private String targetType; // 操作对象类型（如 ITEM, USER, CLAIM）

    @TableField("target_id")
    private Long targetId; // 被操作的对象ID

    private String action; // 操作动作（如 APPROVE, REJECT, DELETE）

    @TableField("from_status")
    private String fromStatus; // 操作前的原状态

    @TableField("to_status")
    private String toStatus; // 操作后的目标状态

    private String remark; // 补充备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
