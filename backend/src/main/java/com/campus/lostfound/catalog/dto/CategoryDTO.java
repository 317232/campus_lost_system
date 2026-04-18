package com.campus.lostfound.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryDTO {

    @Data
    public static class CreateCategoryReq {
        @NotBlank(message = "分类名称不能为空")
        private String name;

        private Integer sortOrder = 0;

        private String status = "ENABLED";
    }

    @Data
    public static class UpdateCategoryReq {
        private Long id;

        @NotBlank(message = "分类名称不能为空")
        private String name;

        private Integer sortOrder;

        private String status;
    }

    @Data
    public static class CategoryResp {
        private Long id;
        private String bizId;
        private String name;
        private Integer sortOrder;
        private String status;
        private LocalDateTime createTime;
    }
}