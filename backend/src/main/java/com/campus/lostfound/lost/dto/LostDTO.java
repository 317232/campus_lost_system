package com.campus.lostfound.lost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LostDTO {

    @Data
    public static class CreateLostReq {
        @NotBlank(message = "物品名称不能为空")
        private String itemName;

        @NotBlank(message = "物品描述不能为空")
        private String description;

        private Long categoryId;

        @NotBlank(message = "丢失地点不能为空")
        private String location;

        private String timeLabel;

        @NotBlank(message = "联系方式不能为空")
        private String contactPhone;

        private String images;
    }

    @Data
    public static class UpdateLostReq {
        private Long id;

        @NotBlank(message = "物品名称不能为空")
        private String itemName;

        @NotBlank(message = "物品描述不能为空")
        private String description;

        private Long categoryId;

        @NotBlank(message = "丢失地点不能为空")
        private String location;

        private String timeLabel;

        @NotBlank(message = "联系方式不能为空")
        private String contactPhone;

        private String images;
    }

    @Data
    public static class LostItemResp {
        private Long id;
        private String bizId;
        private String itemName;
        private String description;
        private Long categoryId;
        private String categoryName;
        private String location;
        private String timeLabel;
        private String contactPhone;
        private String images;
        private String status;
        private Long userId;
        private String userName;
        private LocalDateTime createTime;
    }

    @Data
    public static class LostQueryReq {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String keyword;
        private String status;
        private Long categoryId;
    }
}