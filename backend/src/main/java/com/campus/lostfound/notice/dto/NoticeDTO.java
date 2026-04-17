package com.campus.lostfound.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeDTO {

    @Data
    public static class CreateNoticeReq {
        @NotBlank(message = "标题不能为空")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        private Boolean published = false;
    }

    @Data
    public static class UpdateNoticeReq {
        private Long id;

        @NotBlank(message = "标题不能为空")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        private Boolean published;
    }

    @Data
    public static class NoticeResp {
        private Long id;
        private String title;
        private String content;
        private Boolean published;
        private LocalDateTime publishedAt;
        private LocalDateTime createTime;
    }

    @Data
    public static class NoticeQueryReq {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private Boolean published;
        private String keyword;
    }
}