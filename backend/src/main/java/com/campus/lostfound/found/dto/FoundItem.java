package com.campus.lostfound.found.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FoundItem implements Serializable {
  private static final long serialVersionUID = 1L;
    /**
     * 物品 ID
     */
    private Long id;

    /**
     * 物品名称
     */
    private String itemName;
    /**
     * 详细描述
     */
    private String description;
    /**
     * 物品分类 ID
     */
    private Long categoryId;
    /**
     * 拾取地点
     */
    private String location;
    /**
     * 拾取时间
     */
    private LocalDateTime time;

    /**
     * 图片地址 (前端展示用)
     */
    private String images;
    /**
     * 联系人姓名
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 状态: UNCLAIMED-未认领, CLAIMED-已认领
     */
    private String status;

    /**
     * 发布人 ID
     */
    private Long userId;

    /**
     * 发布时间 (前端展示需要)
     */
    private LocalDateTime createTime;

} 
