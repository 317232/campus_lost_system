package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("item_attachments")
public class ItemAttachment extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private Long itemId; // 关联的物品主表ID

    @TableField("file_url")
    private String fileUrl; // 文件（图片）地址，符合报告中的路径存储要求

    @TableField("file_type")
    private String fileType; // 文件类型

    @TableField("sort_order")
    private Integer sortOrder; // 排序号，用于多图展示控制

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
