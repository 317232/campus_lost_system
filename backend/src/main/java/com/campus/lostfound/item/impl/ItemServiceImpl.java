package com.campus.lostfound.item.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.domain.entity.Category;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.ItemAttachment;
import com.campus.lostfound.domain.entity.ItemAudit;
import com.campus.lostfound.domain.entity.ItemContact;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.ItemAttachmentMapper;
import com.campus.lostfound.mapper.ItemContactMapper;
import com.campus.lostfound.domain.enums.ItemScene;
import com.campus.lostfound.domain.enums.ItemStatus;
import com.campus.lostfound.item.ItemController.*;
import com.campus.lostfound.item.ItemService;
import com.campus.lostfound.item.dto.MatchItem;
import com.campus.lostfound.item.dto.MatchResp;
import com.campus.lostfound.mapper.CategoryMapper;
import com.campus.lostfound.mapper.ItemAuditMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ItemServiceImpl implements ItemService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ItemMapper itemMapper;
    private final ItemAuditMapper itemAuditMapper;
    private final ItemAttachmentMapper itemAttachmentMapper;
    private final ItemContactMapper itemContactMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final SecurityUserUtils securityUserUtils;

    public ItemServiceImpl(ItemMapper itemMapper,
                           ItemAuditMapper itemAuditMapper,
                           ItemAttachmentMapper itemAttachmentMapper,
                           ItemContactMapper itemContactMapper,
                           UserMapper userMapper,
                           CategoryMapper categoryMapper,
                           SecurityUserUtils securityUserUtils) {
        this.itemMapper = itemMapper;
        this.itemAuditMapper = itemAuditMapper;
        this.itemAttachmentMapper = itemAttachmentMapper;
        this.itemContactMapper = itemContactMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public PageResponse<ItemResp> getItemsPage(ItemQueryReq query) {
        Page<Item> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();

        String scene = normalizeScene(query.getScene());
        if (StringUtils.hasText(scene)) {
            wrapper.eq(Item::getScene, scene);
        }

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Item::getItemName, query.getKeyword())
                    .or()
                    .like(Item::getDescription, query.getKeyword())
                    .or()
                    .like(Item::getLocation, query.getKeyword()));
        }

        Long categoryId = resolveCategoryId(query.getCategory());
        if (categoryId != null) {
            wrapper.eq(Item::getCategoryId, categoryId);
        }

        if (StringUtils.hasText(query.getZone())) {
            wrapper.like(Item::getLocation, query.getZone());
        }

        wrapper.eq(Item::getStatus, ItemStatus.PUBLISHED.name());
        wrapper.eq(Item::getIsDelete, 0);

        // 动态排序：默认 create_time DESC
        if ("occurred_at".equalsIgnoreCase(query.getSortBy())) {
            if ("asc".equalsIgnoreCase(query.getSortOrder())) {
                wrapper.orderByAsc(Item::getOccurredAt);
            } else {
                wrapper.orderByDesc(Item::getOccurredAt);
            }
        } else {
            // 默认按 create_time 降序
            if ("asc".equalsIgnoreCase(query.getSortOrder())) {
                wrapper.orderByAsc(Item::getCreateTime);
            } else {
                wrapper.orderByDesc(Item::getCreateTime);
            }
        }

        Page<Item> itemPage = itemMapper.selectPage(page, wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(itemPage.getRecords());
        List<ItemResp> dtoList = itemPage.getRecords().stream()
                .map(item -> convertToResp(item, categoryNames))
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, itemPage.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public ItemResp getItemById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || !Objects.equals(item.getIsDelete(), 0)) {
            return null;
        }
        return convertToResp(item, loadCategoryNames(List.of(item)));
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
        item.setScene(requireScene(request.getScene()));
        item.setStatus(ItemStatus.PENDING_REVIEW.name());
        item.setOwnerId(userId);
        item.setTitle(request.getTitle());
        item.setItemName(request.getItemName());
        item.setCategoryId(requireCategoryId(request.getCategory()));
        item.setLocation(request.getLocation());
        item.setOccurredAt(parseOccurredAt(request.getTimeLabel()));
        item.setVerifyMethod(request.getVerifyMethod());
        item.setDescription(request.getDescription());
        item.setValueTag(request.getValueTag());
        item.setContactVisibility(StringUtils.hasText(request.getContactVisibility())
                ? request.getContactVisibility()
                : "MASKED");

        itemMapper.insert(item);

        // 保存联系方式
        if (StringUtils.hasText(request.getContactValue())) {
            ItemContact contact = new ItemContact();
            contact.setItemId(item.getId());
            contact.setContactType(normalizeContactType(request.getContactType()));
            contact.setContactValue(request.getContactValue());
            contact.setMaskedValue(maskContact(request.getContactValue()));
            contact.setIsPrimary(true);
            itemContactMapper.insert(contact);
        }

        // 保存附件（图片）
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                ItemAttachment attachment = new ItemAttachment();
                attachment.setItemId(item.getId());
                attachment.setFileUrl(request.getImages().get(i));
                attachment.setSortOrder(i);
                attachment.setFileType(determineFileType(request.getImages().get(i)));
                itemAttachmentMapper.insert(attachment);
            }
        }
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

        if (!Objects.equals(item.getOwnerId(), userId)) {
            throw new IllegalArgumentException("无权修改此物品");
        }

        BeanUtils.copyProperties(request, item, "id", "scene", "ownerId", "bizId", "category", "zone", "timeLabel");
        if (StringUtils.hasText(request.getCategory())) {
            item.setCategoryId(requireCategoryId(request.getCategory()));
        }
        if (StringUtils.hasText(request.getTimeLabel())) {
            item.setOccurredAt(parseOccurredAt(request.getTimeLabel()));
        }
        item.setStatus(ItemStatus.PENDING_REVIEW.name());

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

        if (!Objects.equals(item.getOwnerId(), userId)) {
            throw new IllegalArgumentException("无权删除此物品");
        }

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
        wrapper.eq(Item::getIsDelete, 0);

        String normalizedScene = normalizeScene(scene);
        if (StringUtils.hasText(normalizedScene)) {
            wrapper.eq(Item::getScene, normalizedScene);
        }

        wrapper.orderByDesc(Item::getCreateTime);

        Page<Item> itemPage = itemMapper.selectPage(pageObj, wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(itemPage.getRecords());
        List<ItemResp> dtoList = itemPage.getRecords().stream()
                .map(item -> convertToResp(item, categoryNames))
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, itemPage.getTotal(), page, pageSize);
    }

    @Override
    public MatchResp getMatches(Long itemId, Integer limit) {
        Item sourceItem = itemMapper.selectById(itemId);
        if (sourceItem == null) {
            return new MatchResp("", new ArrayList<>());
        }

        String targetScene = ItemScene.LOST.name().equals(sourceItem.getScene())
                ? ItemScene.FOUND.name()
                : ItemScene.LOST.name();

        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getScene, targetScene)
               .eq(Item::getStatus, ItemStatus.PUBLISHED.name())
               .eq(Item::getIsDelete, 0)
               .ne(Item::getId, itemId);

        if (sourceItem.getCategoryId() != null) {
            wrapper.and(w -> w.eq(Item::getCategoryId, sourceItem.getCategoryId())
                    .or()
                    .like(Item::getItemName, sourceItem.getItemName()));
        }

        List<Item> candidates = itemMapper.selectList(wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(candidates);
        List<MatchItem> matches = candidates.stream()
                .map(c -> buildMatchItem(c, calculateMatchScore(sourceItem, c), categoryNames))
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(limit)
                .collect(Collectors.toList());

        MatchResp resp = new MatchResp();
        resp.setScene(targetScene);
        resp.setItems(matches);
        return resp;
    }

    private int calculateMatchScore(Item source, Item target) {
        int score = 0;

        if (source.getCategoryId() != null && source.getCategoryId().equals(target.getCategoryId())) {
            score += 30;
        }

        if (StringUtils.hasText(source.getLocation()) && source.getLocation().equals(target.getLocation())) {
            score += 25;
        }

        if (StringUtils.hasText(source.getItemName()) && StringUtils.hasText(target.getItemName())) {
            String srcName = source.getItemName().toLowerCase();
            String tgtName = target.getItemName().toLowerCase();
            if (tgtName.contains(srcName) || srcName.contains(tgtName) || srcName.equals(tgtName)) {
                score += 25;
            }
        }

        return score;
    }

    private MatchItem buildMatchItem(Item item, int score, Map<Long, String> categoryNames) {
        MatchItem matchItem = new MatchItem();
        matchItem.setId(item.getId());
        matchItem.setBizId(item.getBizId());
        matchItem.setTitle(item.getTitle());
        matchItem.setItemName(item.getItemName());
        matchItem.setCategory(categoryNames.get(item.getCategoryId()));
        matchItem.setZone(item.getLocation());
        matchItem.setLocation(item.getLocation());
        matchItem.setTimeLabel(formatOccurredAt(item.getOccurredAt()));
        matchItem.setThumbnail(null);
        matchItem.setMatchScore(score);
        return matchItem;
    }

    private ItemResp convertToResp(Item item) {
        return convertToResp(item, loadCategoryNames(List.of(item)));
    }

    private ItemResp convertToResp(Item item, Map<Long, String> categoryNames) {
        ItemResp resp = new ItemResp();
        resp.setId(item.getId());
        resp.setBizId(item.getBizId());
        resp.setScene(item.getScene());
        resp.setStatus(item.getStatus());
        resp.setOwnerId(item.getOwnerId());
        resp.setTitle(item.getTitle());
        resp.setItemName(item.getItemName());
        resp.setCategory(categoryNames.get(item.getCategoryId()));
        resp.setCategoryId(item.getCategoryId());
        resp.setCategoryName(categoryNames.get(item.getCategoryId()));
        resp.setZone(item.getLocation());
        resp.setLocation(item.getLocation());
        resp.setTimeLabel(formatOccurredAt(item.getOccurredAt()));
        resp.setVerifyMethod(item.getVerifyMethod());
        resp.setDescription(item.getDescription());
        resp.setValueTag(item.getValueTag());
        resp.setContactVisibility(item.getContactVisibility());
        resp.setCreatedAt(item.getCreateTime());

        if (item.getOwnerId() != null) {
            User owner = userMapper.selectById(item.getOwnerId());
            if (owner != null) {
                resp.setOwnerName(owner.getName());
            }
        }

        resp.setContacts(new ArrayList<>());
        resp.setAttachments(new ArrayList<>());

        return resp;
    }

    @Override
    @Transactional
    public void approveItem(Long itemId, Long auditorId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        String fromStatus = item.getStatus();
        item.setStatus(ItemStatus.PUBLISHED.name());
        item.setPublishedAt(LocalDateTime.now());
        itemMapper.updateById(item);

        insertAudit(itemId, auditorId, "APPROVE", fromStatus, ItemStatus.PUBLISHED.name());
    }

    @Override
    @Transactional
    public void rejectItem(Long itemId, Long auditorId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        String fromStatus = item.getStatus();
        item.setStatus(ItemStatus.REJECTED.name());
        itemMapper.updateById(item);

        insertAudit(itemId, auditorId, "REJECT", fromStatus, ItemStatus.REJECTED.name());
    }

    @Override
    @Transactional
    public void markAsClaimed(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        item.setStatus(ItemStatus.CLAIMED.name());
        item.setClosedAt(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    @Override
    @Transactional
    public void markAsFoundBack(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        item.setStatus(ItemStatus.FOUND_BACK.name());
        item.setClosedAt(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    @Override
    @Transactional
    public void setOffline(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        item.setStatus(ItemStatus.OFFLINE.name());
        itemMapper.updateById(item);
    }

    @Override
    @Transactional
    public void adminDeleteItem(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        itemMapper.deleteById(itemId);
    }

    @Override
    @Transactional
    public void updateItemStatus(Long itemId, String status) {
        Long userId = securityUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        // 权限校验：必须是物品主人或管理员
        boolean isOwner = Objects.equals(item.getOwnerId(), userId);
        boolean isAdmin = securityUserUtils.hasRole("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("无权更新此物品状态");
        }

        // 校验状态值是否合法
        String normalizedStatus = status.trim().toUpperCase();
        ItemStatus validStatus;
        try {
            validStatus = ItemStatus.valueOf(normalizedStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("非法的物品状态: " + status);
        }

        // 业务规则校验
        String currentStatus = item.getStatus();
        if ("PENDING_REVIEW".equals(currentStatus) && !isAdmin) {
            throw new IllegalArgumentException("待审核物品的状态只能由管理员修改");
        }

        item.setStatus(validStatus.name());
        if (validStatus == ItemStatus.CLAIMED || validStatus == ItemStatus.FOUND_BACK || validStatus == ItemStatus.OFFLINE) {
            item.setClosedAt(LocalDateTime.now());
        }
        itemMapper.updateById(item);
    }

    private void insertAudit(Long itemId, Long auditorId, String action, String fromStatus, String toStatus) {
        ItemAudit audit = new ItemAudit();
        audit.setItemId(itemId);
        audit.setAuditorId(auditorId);
        audit.setAuditAction(action);
        audit.setFromStatus(fromStatus);
        audit.setToStatus(toStatus);
        itemAuditMapper.insert(audit);
    }

    private Long requireCategoryId(String category) {
        Long categoryId = resolveCategoryId(category);
        if (categoryId != null) {
            return categoryId;
        }
        Category fallback = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .last("LIMIT 1"));
        if (fallback != null) {
            return fallback.getId();
        }
        Category categoryToCreate = new Category();
        categoryToCreate.setBizId("CAT" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        categoryToCreate.setName(StringUtils.hasText(category) ? category : "默认分类");
        categoryToCreate.setSortOrder(0);
        categoryToCreate.setStatus("ENABLED");
        categoryMapper.insert(categoryToCreate);
        return categoryToCreate.getId();
    }

    private Long resolveCategoryId(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        try {
            return Long.parseLong(category);
        } catch (NumberFormatException ignored) {
            Category matched = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getName, category)
                    .last("LIMIT 1"));
            return matched == null ? null : matched.getId();
        }
    }

    private Map<Long, String> loadCategoryNames(List<Item> items) {
        List<Long> categoryIds = items.stream()
                .map(Item::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private String requireScene(String scene) {
        String normalized = normalizeScene(scene);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("场景仅支持 LOST 或 FOUND");
        }
        return normalized;
    }

    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            return null;
        }
        String normalized = scene.trim().toUpperCase();
        if ("LOST".equals(normalized) || "FOUND".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private LocalDateTime parseOccurredAt(String timeLabel) {
        if (!StringUtils.hasText(timeLabel)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(timeLabel);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(timeLabel, DATE_TIME_FORMATTER);
        }
    }

    private String formatOccurredAt(LocalDateTime occurredAt) {
        return occurredAt == null ? null : occurredAt.format(DATE_TIME_FORMATTER);
    }

    private String maskContact(String contact) {
        if (!StringUtils.hasText(contact)) {
            return null;
        }
        int len = contact.length();
        if (len <= 3) {
            return "*".repeat(len);
        }
        return contact.substring(0, 3) + "*".repeat(len - 3);
    }

    private String normalizeContactType(String contactType) {
        if (!StringUtils.hasText(contactType)) {
            return "PHONE";
        }
        String normalized = contactType.trim().toUpperCase();
        switch (normalized) {
            case "PHONE":
            case "EMAIL":
            case "WECHAT":
            case "QQ":
            case "OTHER":
                return normalized;
            case "手机":
            case "电话":
                return "PHONE";
            case "邮箱":
            case "邮件":
                return "EMAIL";
            case "微信":
                return "WECHAT";
            case "QQ":
                return "QQ";
            case "其他":
                return "OTHER";
            default:
                return "PHONE";
        }
    }

    private String determineFileType(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return "UNKNOWN";
        }
        String lower = fileUrl.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")) {
            return "IMAGE";
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")
                || lower.endsWith(".wmv") || lower.endsWith(".flv")) {
            return "VIDEO";
        }
        if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx")
                || lower.endsWith(".txt") || lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
            return "DOCUMENT";
        }
        return "UNKNOWN";
    }
}
