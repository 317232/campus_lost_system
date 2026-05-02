package com.campus.lostfound.item.dto;

import lombok.Data;

/**
 * 匹配物品响应DTO
 */
@Data
public class MatchItem {
    private Long id;
    private String bizId;
    private String title;
    private String itemName;
    private String category;
    private String zone;
    private String location;
    private String timeLabel;
    private String thumbnail;
    private Integer matchScore;
}
