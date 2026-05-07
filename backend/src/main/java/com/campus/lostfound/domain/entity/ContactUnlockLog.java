package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contact_unlock_logs")
public class ContactUnlockLog extends BaseEntity {

    @TableField("item_id")
    private Long itemId;

    @TableField("viewer_id")
    private Long viewerId;

    private String source;
}
