package com.campus.lostfound.found;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.found.dto.FoundItem;
import com.campus.lostfound.found.service.FoundItemService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/found-items")
public class FoundItemController  {

    private final FoundItemService foundItemService;

    public FoundItemController(FoundItemService foundItemService) {
        this.foundItemService = foundItemService;
    }

    /**
     * 获取拾物列表（建议加入分页和条件查询）
     */
    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        // 调用 Service 层的真实分页查询逻辑
        Page<FoundItem> pageResult = foundItemService.getFoundItemsPage(pageNum, pageSize, keyword);
        PageResponse<FoundItem> pageResponse = new PageResponse<>(pageResult.getRecords(), pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
        return ApiResponse.success(pageResponse);
    }

    /**
     * 获取拾物详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        // 从数据库根据 ID 查询真实数据
        FoundItem item = foundItemService.getFoundItemById(id);
        if (item == null) {
            return ApiResponse.failure(ResultCode.NOT_FOUND, "该拾物信息不存在");
        }
        return ApiResponse.success(item);
    }

    /**
     * 发布新的拾物信息
     */
    @PostMapping
    public ApiResponse<?> create(@RequestBody FoundItem foundItem) {
        // TODO: 这里可以从上下文中获取当前登录用户的 ID，并赋值给 foundItem.setUserId()
        boolean saved = foundItemService.createFoundItem(foundItem);
        if (saved) {
            return ApiResponse.success("发布成功", foundItem);
        }
        return ApiResponse.failure(ResultCode.INTERNAL_ERROR, "发布失败，请稍后重试");
    }
    /**
     * 更新拾物信息
     */
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody FoundItem foundItem) {
        foundItem.setId(id); // 确保更新的是指定的 ID
        boolean updated = foundItemService.updateFoundItem(foundItem);
        if (updated) {
            return ApiResponse.success("更新成功", foundItem);
        }
        return ApiResponse.failure(ResultCode.INTERNAL_ERROR, "更新失败，物品可能不存在");
    }

    /**
     * 删除拾物信息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        boolean deleted = foundItemService.deleteFoundItem(id);
        if (deleted) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.failure(ResultCode.INTERNAL_ERROR, "删除失败，物品可能不存在");
    }
}
