package com.campus.lostfound.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.common.service.ContactService;
import com.campus.lostfound.domain.entity.ContactUnlockLog;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.ItemContact;
import com.campus.lostfound.mapper.ContactUnlockLogMapper;
import com.campus.lostfound.mapper.ItemContactMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ContactServiceImpl implements ContactService {

    private final ItemContactMapper itemContactMapper;
    private final ContactUnlockLogMapper contactUnlockLogMapper;
    private final ItemMapper itemMapper;
    private final SecurityUserUtils securityUserUtils;

    public ContactServiceImpl(ItemContactMapper itemContactMapper,
                             ContactUnlockLogMapper contactUnlockLogMapper,
                             ItemMapper itemMapper,
                             SecurityUserUtils securityUserUtils) {
        this.itemContactMapper = itemContactMapper;
        this.contactUnlockLogMapper = contactUnlockLogMapper;
        this.itemMapper = itemMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public String unlockContact(Long itemId, Long viewerId, String source) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        // Get contact info
        List<ItemContact> contacts = itemContactMapper.selectList(
            new LambdaQueryWrapper<ItemContact>().eq(ItemContact::getItemId, itemId));

        if (contacts.isEmpty()) {
            return null;
        }

        // Check if item allows contact visibility (UNMASKED)
        // Only record unlock log for masked items (not publicly visible)
        if (!"UNMASKED".equals(item.getContactVisibility())) {
            recordUnlockLog(itemId, viewerId, source);
        }

        // Return the first contact's value (real contact)
        return contacts.get(0).getContactValue();
    }

    @Override
    public List<ItemContact> getMaskedContacts(Long itemId) {
        return itemContactMapper.selectList(
            new LambdaQueryWrapper<ItemContact>().eq(ItemContact::getItemId, itemId));
    }

    @Override
    public Map<String, String> unlockContactFull(Long itemId, Long viewerId, String source) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }

        // Get contact info
        List<ItemContact> contacts = itemContactMapper.selectList(
            new LambdaQueryWrapper<ItemContact>().eq(ItemContact::getItemId, itemId));

        if (contacts.isEmpty()) {
            return null;
        }

        // Check if item allows contact visibility (UNMASKED)
        // Only record unlock log for masked items (not publicly visible)
        if (!"UNMASKED".equals(item.getContactVisibility())) {
            recordUnlockLog(itemId, viewerId, source);
        }

        // Return the first contact's full info (real contact)
        ItemContact primaryContact = contacts.get(0);
        Map<String, String> result = new java.util.HashMap<>();
        result.put("contactType", primaryContact.getContactType() != null ? primaryContact.getContactType() : "手机");
        result.put("contactValue", primaryContact.getContactValue());
        return result;
    }

    private void recordUnlockLog(Long itemId, Long viewerId, String source) {
        Long actualViewerId = viewerId != null ? viewerId : securityUserUtils.getCurrentUserId();
        if (actualViewerId == null) {
            return; // Anonymous user, skip logging
        }

        ContactUnlockLog log = new ContactUnlockLog();
        log.setItemId(itemId);
        log.setViewerId(actualViewerId);
        log.setSource(source != null ? source : "UNKNOWN");
        contactUnlockLogMapper.insert(log);
    }
}