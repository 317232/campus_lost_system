package com.campus.lostfound.item.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.ItemAudit;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.item.ItemController.*;
import com.campus.lostfound.item.ItemService;
import com.campus.lostfound.mapper.ItemAuditMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemAuditMapper itemAuditMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SecurityUserUtils securityUserUtils;

    @Override
    public PageResponse<ItemResp> getItemsPage(ItemQueryReq query) {
        Page<Item> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();

        // Scene filter (lost/found)
        if (StringUtils.hasText(query.getScene())) {
            wrapper.eq(Item::getScene, query.getScene());
        }

        // Keyword search
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Item::getItemName, query.getKeyword())
                    .or()
                    .like(Item::getDescription, query.getKeyword()));
        }

        // Category filter
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Item::getCategory, query.getCategory());
        }

        // Zone filter
        if (StringUtils.hasText(query.getZone())) {
            wrapper.eq(Item::getZone, query.getZone());
        }

        // Status filter
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Item::getStatus, query.getStatus());
        }

        // Only show published items for public queries
        wrapper.eq(Item::getStatus, "PUBLISHED");

        wrapper.orderByDesc(Item::getCreatedAt);

        Page<Item> itemPage = itemMapper.selectPage(page, wrapper);

        List<ItemResp> dtoList = itemPage.getRecords().stream()
                .map(this::convertToResp)
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, itemPage.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public ItemResp getItemById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            return null;
        }
        return convertToResp(item);
    }

    @Override
    @Transactional
    public void createItem(CreateItemReq request) {
        Long userId = securityUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        Item item = new Item();
        item.setBizId(UUID.randomUUID().toString().replace("-", ""));
        item.setScene(request.getScene());
        item.setStage("active");
        item.setStatus("PENDING_REVIEW"); // 审核中状态
        item.setOwnerId(userId);
        item.setTitle(request.getTitle());
        item.setItemName(request.getItemName());
        item.setCategory(request.getCategory());
        item.setZone(request.getZone());
        item.setLocation(request.getLocation());
        item.setTimeLabel(request.getTimeLabel());
        item.setVerifyMethod(request.getVerifyMethod());
        item.setDescription(request.getDescription());
        item.setValueTag(request.getValueTag());
        item.setContactVisibility(request.getContactVisibility() != null ?
                request.getContactVisibility() : "MASKED");

        itemMapper.insert(item);

        // TODO: Create ItemContact and ItemAttachment records
        // 需要创建 ItemContactMapper 和 ItemAttachmentMapper
    }

    @Override
    @Transactional
    public boolean updateItem(UpdateItemReq request) {
        Long userId = securityUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        Item item = itemMapper.selectById(request.getId());
        if (item == null) {
            return false;
        }

        // Check ownership
        if (!item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此物品");
        }

        BeanUtils.copyProperties(request, item, "id", "scene", "ownerId", "bizId");

        // Reset to pending review after modification
        item.setStatus("PENDING_REVIEW");

        return itemMapper.updateById(item) > 0;
    }

    @Override
    @Transactional
    public boolean deleteItem(Long id) {
        Long userId = securityUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        Item item = itemMapper.selectById(id);
        if (item == null) {
            return false;
        }

        // Check ownership
        if (!item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此物品");
        }

        // Logical delete via MyBatis-Plus @TableLogic
        return itemMapper.deleteById(id) > 0;
    }

    @Override
    public PageResponse<ItemResp> getMyItems(Integer page, Integer pageSize, String scene) {
        Long userId = securityUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        Page<Item> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getOwnerId, userId);
        
        // Optional scene filter (lost/found)
        if (StringUtils.hasText(scene)) {
            wrapper.eq(Item::getScene, scene);
        }
        
        wrapper.orderByDesc(Item::getCreatedAt);

        Page<Item> itemPage = itemMapper.selectPage(pageObj, wrapper);

        List<ItemResp> dtoList = itemPage.getRecords().stream()
                .map(this::convertToResp)
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, itemPage.getTotal(), page, pageSize);
    }

    private ItemResp convertToResp(Item item) {
        ItemResp resp = new ItemResp();
        resp.setId(item.getId());
        resp.setBizId(item.getBizId());
        resp.setScene(item.getScene());
        resp.setStage(item.getStage());
        resp.setStatus(item.getStatus());
        resp.setOwnerId(item.getOwnerId());
        resp.setTitle(item.getTitle());
        resp.setItemName(item.getItemName());
        resp.setCategory(item.getCategory());
        resp.setZone(item.getZone());
        resp.setLocation(item.getLocation());
        resp.setTimeLabel(item.getTimeLabel());
        resp.setVerifyMethod(item.getVerifyMethod());
        resp.setDescription(item.getDescription());
        resp.setValueTag(item.getValueTag());
        resp.setContactVisibility(item.getContactVisibility());
        resp.setCreatedAt(item.getCreatedAt());

        // Load owner name
        if (item.getOwnerId() != null) {
            User owner = userMapper.selectById(item.getOwnerId());
            if (owner != null) {
                resp.setOwnerName(owner.getName());
            }
        }

        // TODO: Load contacts and attachments from ItemContactMapper/ItemAttachmentMapper
        resp.setContacts(new ArrayList<>());
        resp.setAttachments(new ArrayList<>());

        return resp;
    }
}