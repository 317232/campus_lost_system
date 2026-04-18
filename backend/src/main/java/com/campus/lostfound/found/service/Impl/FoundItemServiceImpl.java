package com.campus.lostfound.found.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.found.dto.FoundItem;
import com.campus.lostfound.found.service.FoundItemService;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FoundItemServiceImpl implements FoundItemService {

    private final ItemMapper itemMapper;
    private final SecurityUserUtils securityUserUtils;

    public FoundItemServiceImpl(ItemMapper itemMapper, SecurityUserUtils securityUserUtils) {
        this.itemMapper = itemMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public Page<FoundItem> getFoundItemsPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<Item> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getScene, "found");

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like(Item::getItemName, keyword)
                    .or()
                    .like(Item::getDescription, keyword));
        }

        Page<Item> itemPage = itemMapper.selectPage(page, queryWrapper);

        Page<FoundItem> dtoPage = new Page<>(
            itemPage.getCurrent(),
            itemPage.getSize(),
            itemPage.getTotal()
        );

        List<FoundItem> dtoList = itemPage.getRecords()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public FoundItem getFoundItemById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || !"found".equals(item.getScene())) {
            return null;
        }
        return convertToDto(item);
    }

    @Override
    public boolean createFoundItem(FoundItem foundItemDto) {
        validateCreatePayload(foundItemDto);

        Long ownerId = securityUserUtils.getCurrentUserId();
        if (ownerId == null) {
            throw new IllegalArgumentException("请先登录后再发布拾物信息");
        }

        Item item = convertToEntity(foundItemDto);
        item.setBizId(generateBizId());
        item.setScene("found");
        item.setStage("active");
        item.setAuditStatus("PENDING");
        item.setStatus("PUBLISHED");
        item.setOwnerId(ownerId);
        item.setTitle(StringUtils.hasText(foundItemDto.getItemName()) ? foundItemDto.getItemName().trim() : "拾物招领");
        item.setTimeLabel(foundItemDto.getTime() == null ? "待补充" : foundItemDto.getTime().toString());
        return itemMapper.insert(item) > 0;
    }

    @Override
    public boolean updateFoundItem(FoundItem foundItemDto) {
        if (foundItemDto.getId() == null) {
            return false;
        }
        Item item = itemMapper.selectById(foundItemDto.getId());
        if (item == null || !"found".equals(item.getScene())) {
            return false;
        }
        BeanUtils.copyProperties(foundItemDto, item, "id", "bizId", "scene", "ownerId", "createdAt");
        return itemMapper.updateById(item) > 0;
    }

    @Override
    public boolean deleteFoundItem(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || !"found".equals(item.getScene())) {
            return false;
        }
        return itemMapper.deleteById(id) > 0;
    }

    private void validateCreatePayload(FoundItem dto) {
        if (!StringUtils.hasText(dto.getItemName())) {
            throw new IllegalArgumentException("物品名称不能为空");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new IllegalArgumentException("物品描述不能为空");
        }
        if (!StringUtils.hasText(dto.getLocation())) {
            throw new IllegalArgumentException("拾取地点不能为空");
        }
    }

    private FoundItem convertToDto(Item item) {
        FoundItem dto = new FoundItem();
        dto.setId(item.getId());
        dto.setItemName(item.getItemName());
        dto.setDescription(item.getDescription());
        dto.setLocation(item.getLocation());
        dto.setContactName(item.getContactVisibility());
        dto.setStatus(item.getStatus());
        dto.setUserId(item.getOwnerId());
        dto.setCreateTime(item.getCreatedAt());
        return dto;
    }

    private Item convertToEntity(FoundItem dto) {
        Item item = new Item();
        if (dto.getId() != null) {
            item.setId(dto.getId());
        }
        item.setItemName(dto.getItemName() == null ? null : dto.getItemName().trim());
        item.setDescription(dto.getDescription() == null ? null : dto.getDescription().trim());
        item.setLocation(dto.getLocation() == null ? null : dto.getLocation().trim());
        item.setContactVisibility(StringUtils.hasText(dto.getContactName()) ? dto.getContactName().trim() : "UNMASKED");
        return item;
    }

    private String generateBizId() {
        return "FND" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
