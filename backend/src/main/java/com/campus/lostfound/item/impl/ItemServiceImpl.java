package com.campus.lostfound.item.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.ItemAudit;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.item.ItemController.*;
import com.campus.lostfound.item.ItemService;
import com.campus.lostfound.item.dto.MatchItem;
import com.campus.lostfound.item.dto.MatchResp;
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

    @Override
    public MatchResp getMatches(Long itemId, Integer limit) {
        Item sourceItem = itemMapper.selectById(itemId);
        if (sourceItem == null) {
            return new MatchResp("", new ArrayList<>());
        }

        // 确定相反的场景
        String targetScene = "lost".equals(sourceItem.getScene()) ? "found" : "lost";

        // 查询候选物品：相反场景 + 同分类 + 已发布状态
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getScene, targetScene)
               .eq(Item::getStatus, "PUBLISHED")
               // 排除自己
               .ne(Item::getId, itemId);

        // 如果有分类，优先匹配同分类
        if (StringUtils.hasText(sourceItem.getCategory())) {
            wrapper.and(w -> w.eq(Item::getCategory, sourceItem.getCategory())
                    .or()
                    .like(Item::getItemName, sourceItem.getItemName()));
        }

        List<Item> candidates = itemMapper.selectList(wrapper);

        // 计算匹配分并排序
        List<MatchItem> matches = candidates.stream()
                .map(c -> {
                    int score = calculateMatchScore(sourceItem, c);
                    return buildMatchItem(c, score);
                })
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(limit)
                .collect(Collectors.toList());

        MatchResp resp = new MatchResp();
        resp.setScene(targetScene);
        resp.setItems(matches);
        return resp;
    }

    /**
     * 计算匹配分
     * - 分类相同 +30
     * - 地点相同 +25
     * - 名称包含 +25
     * - 时间7天内 +20
     */
    private int calculateMatchScore(Item source, Item target) {
        int score = 0;

        // 分类相同
        if (StringUtils.hasText(source.getCategory()) && 
            source.getCategory().equals(target.getCategory())) {
            score += 30;
        }

        // 地点相同
        if (StringUtils.hasText(source.getZone()) && 
            source.getZone().equals(target.getZone())) {
            score += 25;
        }

        // 名称包含/相似
        if (StringUtils.hasText(source.getItemName()) && 
            StringUtils.hasText(target.getItemName())) {
            String srcName = source.getItemName().toLowerCase();
            String tgtName = target.getItemName().toLowerCase();
            if (tgtName.contains(srcName) || srcName.contains(tgtName) ||
                srcName.equals(tgtName)) {
                score += 25;
            }
        }

        // TODO: 时间相近计算

        return score;
    }

    private MatchItem buildMatchItem(Item item, int score) {
        MatchItem matchItem = new MatchItem();
        matchItem.setId(item.getId());
        matchItem.setBizId(item.getBizId());
        matchItem.setTitle(item.getTitle());
        matchItem.setItemName(item.getItemName());
        matchItem.setCategory(item.getCategory());
        matchItem.setZone(item.getZone());
        matchItem.setLocation(item.getLocation());
        matchItem.setTimeLabel(item.getTimeLabel());
        matchItem.setMatchScore(score);
        // 缩略图暂时设为空，前端可使用附件
        matchItem.setThumbnail(null);
        return matchItem;
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