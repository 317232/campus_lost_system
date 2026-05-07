package com.campus.lostfound.item;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.common.service.ContactService;
import com.campus.lostfound.domain.entity.Category;
import com.campus.lostfound.item.dto.MatchResp;
import com.campus.lostfound.mapper.CategoryMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final ContactService contactService;
    private final SecurityUserUtils securityUserUtils;
    private final CategoryMapper categoryMapper;

    public ItemController(ItemService itemService, ContactService contactService, SecurityUserUtils securityUserUtils, CategoryMapper categoryMapper) {
        this.itemService = itemService;
        this.contactService = contactService;
        this.securityUserUtils = securityUserUtils;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<ItemResp>> list(ItemQueryReq query) {
        PageResponse<ItemResp> page = itemService.getItemsPage(query);
        return ApiResponse.success(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResp>> detail(@PathVariable Long id) {
        ItemResp item = itemService.getItemById(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "物品不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody CreateItemReq request) {
        try {
            itemService.createItem(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("发布成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @Valid @RequestBody UpdateItemReq request) {
        request.setId(id);
        try {
            boolean updated = itemService.updateItem(request);
            if (updated) {
                return ResponseEntity.ok(ApiResponse.success("更新成功", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "物品不存在"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        boolean deleted = itemService.deleteItem(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "物品不存在"));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<ItemResp>> myItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String scene) {
        PageResponse<ItemResp> result = itemService.getMyItems(page, pageSize, scene);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/matches")
    public ApiResponse<MatchResp> getMatches(
            @PathVariable Long id,
            @RequestParam(defaultValue = "3") Integer limit) {
        MatchResp matches = itemService.getMatches(id, limit);
        return ApiResponse.success(matches);
    }

    @GetMapping("/{id}/contact")
    public ResponseEntity<ApiResponse<Map<String, String>>> unlockContact(
            @PathVariable Long id,
            @RequestParam(defaultValue = "DETAIL_PAGE") String source) {
        try {
            // 从安全上下文获取当前用户ID，确保日志可关联
            Long viewerId = securityUserUtils.getCurrentUserId();
            Map<String, String> contactInfo = contactService.unlockContactFull(id, viewerId, source);
            if (contactInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ResultCode.NOT_FOUND, "联系方式不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(contactInfo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 物品主人状态变更（本人或管理员）
    // ═══════════════════════════════════════════════════════════════

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateItemStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemStatusReq request) {
        try {
            itemService.updateItemStatus(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    // ========== Request DTOs ==========

    @lombok.Data
    public static class CreateItemReq {
        @jakarta.validation.constraints.NotBlank(message = "场景不能为空")
        private String scene; // lost/found

        private String title;

        @jakarta.validation.constraints.NotBlank(message = "物品名称不能为空")
        private String itemName;

        private String category;

        public void setCategoryId(Long categoryId) {
            this.category = categoryId == null ? null : String.valueOf(categoryId);
        }

        public Long getCategoryId() {
            if (category == null || category.isBlank()) {
                return null;
            }
            try {
                return Long.parseLong(category);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        private String zone;

        @jakarta.validation.constraints.NotBlank(message = "地点不能为空")
        private String location;

        private String timeLabel;
        private String verifyMethod;
        private String description;
        private String valueTag;

        private String contactVisibility = "MASKED";
        private String contactType;
        private String contactValue;
        private List<String> images;
    }

    @lombok.Data
    public static class UpdateItemReq {
        @jakarta.validation.constraints.NotNull(message = "ID不能为空")
        private Long id;

        private String title;
        private String itemName;
        private String category;

        public void setCategoryId(Long categoryId) {
            this.category = categoryId == null ? null : String.valueOf(categoryId);
        }

        public Long getCategoryId() {
            if (category == null || category.isBlank()) {
                return null;
            }
            try {
                return Long.parseLong(category);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        private String zone;
        private String location;
        private String timeLabel;
        private String verifyMethod;
        private String description;
        private String valueTag;

        private String contactVisibility;
        private String contactType;
        private String contactValue;
        private List<String> images;
    }

    @lombok.Data
    public static class ItemQueryReq {
        private String scene;
        private String keyword;
        private String category;
        private String zone;
        private String status;
        @jakarta.validation.constraints.Min(value = 1, message = "页码最小为1")
        private Integer page = 1;
        @jakarta.validation.constraints.Min(value = 1, message = "每页数量最小为1")
        private Integer pageSize = 10;
        // 排序参数：create_time（默认）, occurred_at
        private String sortBy = "create_time";
        // 排序方向：desc（默认）, asc
        private String sortOrder = "desc";
    }

    /** 物品主人/管理员更新物品状态请求 */
    @lombok.Data
    public static class UpdateItemStatusReq {
        @jakarta.validation.constraints.NotBlank(message = "状态不能为空")
        private String status;
    }

    // ========== Response DTO ==========

    @lombok.Data
    public static class ItemResp {
        private Long id;
        private String bizId;
        private String scene;
        private String status;
        private Long ownerId;
        private String ownerName;
        private String title;
        private String itemName;
        private String category;
        private Long categoryId;
        private String categoryName;
        private String zone;
        private String location;
        private String timeLabel;
        private String verifyMethod;
        private String description;
        private String valueTag;
        private String contactVisibility;
        private List<ContactResp> contacts;
        private List<AttachmentResp> attachments;
        private java.time.LocalDateTime createdAt;
    }

    @lombok.Data
    public static class ContactResp {
        private String contactType;
        private String contactValue;
    }

    @lombok.Data
    public static class AttachmentResp {
        private String fileUrl;
        private String fileType;
    }
}
