package com.campus.lostfound.domain.entity;

import java.time.LocalDateTime;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("annoucements")
public class Annoucement extends BaseEntity {

    @TableField("biz_id")
    private String bizId; // 公告业务编号

    private String title; // 公告标题

    private String content; // 公告内容

    private String status; // 状态：PUBLISHED/OFFLINE

    @TableField("publisher_id")
    private Long publisherId; // 发布人ID

    @TableField("published_at")
    private LocalDateTime publishedAt; // 发布时间

}
