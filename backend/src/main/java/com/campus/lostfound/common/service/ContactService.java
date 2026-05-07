package com.campus.lostfound.common.service;

import com.campus.lostfound.domain.entity.ItemContact;

import java.util.Map;

/**
 * 联系方式服务
 * 处理联系方式的脱敏、解锁日志记录等
 */
public interface ContactService {

    /**
     * 解锁联系方式（用户查看真实联系方式）
     * @param itemId 物品ID
     * @param viewerId 查看人ID
     * @param source 来源（DETAIL_PAGE/LIST_PAGE）
     * @return 解锁后的联系方式
     */
    String unlockContact(Long itemId, Long viewerId, String source);

    /**
     * 解锁联系方式，返回完整信息（包含contactType和contactValue）
     * @param itemId 物品ID
     * @param viewerId 查看人ID
     * @param source 来源（DETAIL_PAGE/LIST_PAGE）
     * @return 包含contactType和contactValue的Map
     */
    Map<String, String> unlockContactFull(Long itemId, Long viewerId, String source);

    /**
     * 获取物品的脱敏联系方式（不记录日志）
     * @param itemId 物品ID
     * @return 脱敏后的联系方式列表
     */
    java.util.List<ItemContact> getMaskedContacts(Long itemId);
}