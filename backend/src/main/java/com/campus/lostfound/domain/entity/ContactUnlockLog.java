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
@TableName("contact_unlock_logs")
public class ContactUnlockLog extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private Long itemId;

    @TableField("viewer_id")
    private Long viewerId;

    private String source; // DETAIL_PAGE / LIST_PAGE

    @TableField("created_at")
    private LocalDateTime createdAt;
}