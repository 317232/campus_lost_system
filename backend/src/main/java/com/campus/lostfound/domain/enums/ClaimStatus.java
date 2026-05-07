package com.campus.lostfound.domain.enums;

/**
 * 认领申请状态
 */
public enum ClaimStatus {
    APPLIED,         // 已申请
    CHECKING,        // 描述核对中
    REVIEW_PENDING,  // 待最终审核
    APPROVED,        // 审核通过
    REJECTED,        // 审核拒绝
    CANCELLED        // 已取消
}