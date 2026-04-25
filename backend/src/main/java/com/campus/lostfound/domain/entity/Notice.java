package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告实体 - 对应 init.sql announcements 表
 * 注意：Notice 映射到 announcements 表（不是 notice 表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcements")
public class Notice extends BaseEntity {

    @TableField("biz_id")
    private String bizId; // 公告业务编号

    private String title; // 公告标题

    private String content; // 公告内容

    private String status; // 状态：PUBLISHED/OFFLINE

    @TableField("publisher_id")
    private Long publisherId; // 发布人ID

    @TableField("published_at")
    private LocalDateTime publishedAt; // 发布时间

    // ===== 兼容旧代码的 getter/setter =====

    /**
     * @deprecated Use getStatus() + "PUBLISHED".equals(status) instead
     */
    @Deprecated
    public Boolean getPublished() {
        return "PUBLISHED".equals(status);
    }

    /**
     * @deprecated Use setStatus() instead
     */
    @Deprecated
    public void setPublished(Boolean published) {
        this.status = (published != null && published) ? "PUBLISHED" : "OFFLINE";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }
}