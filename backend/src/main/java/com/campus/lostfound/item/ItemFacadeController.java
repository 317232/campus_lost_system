package com.campus.lostfound.item;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.common.api.ResultCode;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.found.dto.FoundItem;
import com.campus.lostfound.found.service.FoundItemService;
import com.campus.lostfound.lost.dto.LostDTO;
import com.campus.lostfound.lost.service.LostItemService;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.user.dto.UserItemSummaryResponse;
import com.campus.lostfound.user.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemFacadeController {

    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final ProfileService profileService;
    private final ItemMapper itemMapper;

    public ItemFacadeController(LostItemService lostItemService,
                                FoundItemService foundItemService,
                                ProfileService profileService,
                                ItemMapper itemMapper) {
        this.lostItemService = lostItemService;
        this.foundItemService = foundItemService;
        this.profileService = profileService;
        this.itemMapper = itemMapper;
    }

    @PostMapping("/lost")
    public ApiResponse<Void> publishLost(@Valid @RequestBody LostDTO.CreateLostReq request) {
        lostItemService.createLostItem(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/found")
    public ApiResponse<Void> publishFound(@Valid @RequestBody PublishFoundReq request) {
        FoundItem foundItem = new FoundItem();
        writeField(foundItem, "itemName", request.itemName());
        writeField(foundItem, "description", request.description());
        writeField(foundItem, "location", request.location());
        writeField(foundItem, "time", request.time());
        writeField(foundItem, "contactName", request.contactPhone());
        foundItemService.createFoundItem(foundItem);
        return ApiResponse.success(null);
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<ItemListResp>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category) {

        Page<Item> page = itemMapper.selectPage(new Page<>(pageNum, pageSize),
            new QueryWrapper<Item>()
                .select("id", "biz_id", "scene", "item_name", "category", "location", "status", "created_at")
                .like(keyword != null && !keyword.isBlank(), "item_name", keyword)
                .eq(category != null && !category.isBlank(), "category", category)
                .orderByDesc("id"));

        List<ItemListResp> records = new ArrayList<>();
        List<Map<String, Object>> mapRows = itemMapper.selectMaps(new QueryWrapper<Item>()
            .select("id", "biz_id", "scene", "item_name", "category", "location", "status", "created_at")
            .like(keyword != null && !keyword.isBlank(), "item_name", keyword)
            .eq(category != null && !category.isBlank(), "category", category)
            .orderByDesc("id")
            .last("LIMIT " + pageSize + " OFFSET " + (Math.max(pageNum, 1) - 1) * pageSize));

        for (Map<String, Object> row : mapRows) {
            records.add(toResp(row));
        }

        return ApiResponse.success(new PageResponse<>(records, page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        List<Map<String, Object>> rows = itemMapper.selectMaps(new QueryWrapper<Item>()
            .select("id", "biz_id", "scene", "item_name", "category", "location", "status", "created_at")
            .eq("id", id)
            .last("LIMIT 1"));
        if (rows.isEmpty()) {
            return ApiResponse.failure(ResultCode.NOT_FOUND, "物品不存在");
        }
        return ApiResponse.success(toResp(rows.get(0)));
    }

    @GetMapping("/my")
    public ApiResponse<List<UserItemSummaryResponse>> myItems() {
        List<UserItemSummaryResponse> all = new ArrayList<>();
        all.addAll(profileService.listMyLostItems());
        all.addAll(profileService.listMyFoundItems());
        return ApiResponse.success(all);
    }

    private ItemListResp toResp(Map<String, Object> row) {
        return new ItemListResp(
            asLong(row.get("id")),
            asString(row.get("biz_id")),
            asString(row.get("scene")),
            asString(row.get("item_name")),
            asString(row.get("category")),
            asString(row.get("location")),
            asString(row.get("status")),
            asDateTime(row.get("created_at"))
        );
    }

    private void writeField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("字段写入失败: " + fieldName, e);
        }
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime asDateTime(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        return null;
    }

    public record PublishFoundReq(
        @NotBlank(message = "物品名称不能为空") String itemName,
        @NotBlank(message = "描述不能为空") String description,
        @NotBlank(message = "地点不能为空") String location,
        LocalDateTime time,
        String contactPhone
    ) {
    }

    public record ItemListResp(Long id, String bizId, String scene, String itemName, String category,
                               String location, String status, LocalDateTime createdAt) {
    }
}
