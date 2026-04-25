package com.campus.lostfound.user.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {

    @Data
    public static class UserResp {
        private Long id;
        private String studentNo;
        private String name;
        private String major;
        private String phone;
        private String email;
        private String avatarUrl;
        private String status;
        private String roleCode;
        private String roleName;
        private List<String> permissions;
        private LocalDateTime createTime;
    }

    @Data
    public static class UserItemResp {
        private Long id;
        private String bizId;
        private String title;
        private String itemName;
        private String category;
        private String location;
        private String status;
        private List<String> imageUrls;
        private LocalDateTime createTime;
        private String description;
        private String contact;
    }

    @Data
    public static class UserStatisticsResp {
        private Long userId;
        private String userName;
        private Long totalPosts;
        private Long approvedCount;
        private Long pendingCount;
        private Long rejectedCount;
        private Double approvalRate;
    }
}