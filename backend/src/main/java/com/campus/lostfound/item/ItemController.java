package com.campus.lostfound.item;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.common.service.ContactService;
import com.campus.lostfound.item.dto.MatchResp;
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

    public ItemController(ItemService itemService, ContactService contactService, SecurityUserUtils securityUserUtils) {
        this.itemService = itemService;
        this.contactService = contactService;
        this.securityUserUtils = securityUserUtils;
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
            String contactValue = contactService.unlockContact(id, viewerId, source);
            if (contactValue == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ResultCode.NOT_FOUND, "联系方式不存在"));
            }
            Map<String, String> resp = new java.util.HashMap<>();
            resp.put("contactType", "手机");
            resp.put("contactValue", contactValue);
            return ResponseEntity.ok(ApiResponse.success(resp));
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
    }

    // ========== Response DTO ==========

    @lombok.Data
    public static class ItemResp {
        private Long id;
        private String bizId;
        private String scene;
        private String stage;
        private String status;
        private Long ownerId;
        private String ownerName;
        private String title;
        private String itemName;
        private String category;
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
