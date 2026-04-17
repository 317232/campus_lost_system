package com.campus.lostfound.lost;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.lost.dto.LostDTO;
import com.campus.lostfound.lost.service.LostItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    public LostItemController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LostDTO.LostItemResp>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<LostDTO.LostItemResp> page = lostItemService.getLostItemsPage(pageNum, pageSize, keyword);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LostDTO.LostItemResp>> detail(@PathVariable Long id) {
        LostDTO.LostItemResp item = lostItemService.getLostItemById(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "该失物信息不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody LostDTO.CreateLostReq request) {
        try {
            lostItemService.createLostItem(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @Valid @RequestBody LostDTO.UpdateLostReq request) {
        request.setId(id);
        try {
            boolean updated = lostItemService.updateLostItem(request);
            if (updated) {
                return ResponseEntity.ok(ApiResponse.success(null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "更新失败，物品可能不存在"));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        boolean deleted = lostItemService.deleteLostItem(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "删除失败，物品可能不存在"));
    }
}