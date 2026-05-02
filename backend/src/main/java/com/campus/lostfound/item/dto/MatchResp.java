package com.campus.lostfound.item.dto;

import lombok.Data;

import java.util.List;

/**
 * 匹配响应DTO
 */
@Data
public class MatchResp {
    private String scene;
    private List<MatchItem> items;

    public MatchResp() {}

    public MatchResp(String scene, List<MatchItem> items) {
        this.scene = scene;
        this.items = items;
    }
}
