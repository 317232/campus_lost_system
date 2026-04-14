package com.campus.lostfound.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.lostfound.domain.enums.AuditStatus;
import com.campus.lostfound.domain.enums.LostItemStatus;
import java.time.LocalDateTime;

@TableName("lost_item")
public class LostItem extends BaseEntity {

    private Long userId;
    private Long categoryId;
    private String itemName;
    private String description;
    private String lostLocation;
    private LocalDateTime lostTime;
    private String contactInfo;
    private String imageUrl;
    private AuditStatus auditStatus;
    private LostItemStatus itemStatus;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLostLocation() {
        return lostLocation;
    }

    public void setLostLocation(String lostLocation) {
        this.lostLocation = lostLocation;
    }

    public LocalDateTime getLostTime() {
        return lostTime;
    }

    public void setLostTime(LocalDateTime lostTime) {
        this.lostTime = lostTime;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public AuditStatus getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(AuditStatus auditStatus) {
        this.auditStatus = auditStatus;
    }

    public LostItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(LostItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }
}
