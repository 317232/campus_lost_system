package com.campus.lostfound.lost.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.lost.dto.LostDTO;
import com.campus.lostfound.lost.service.LostItemService;
import com.campus.lostfound.mapper.ItemMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LostItemServiceImpl implements LostItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public Page<LostDTO.LostItemResp> getLostItemsPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<Item> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getScene, "lost");
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like(Item::getItemName, keyword)
                    .or()
                    .like(Item::getDescription, keyword));
        }
        Page<Item> itemPage = itemMapper.selectPage(page, queryWrapper);
        Page<LostDTO.LostItemResp> dtoPage = new Page<>(
            itemPage.getCurrent(),
            itemPage.getSize(),
            itemPage.getTotal()
        );
        List<LostDTO.LostItemResp> dtoList = itemPage.getRecords()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public LostDTO.LostItemResp getLostItemById(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || !"lost".equals(item.getScene())) {
            return null;
        }
        return convertToDto(item);
    }

    @Override
    public void createLostItem(LostDTO.CreateLostReq request) {
        Item item = convertToEntity(request);
        item.setScene("lost");
        item.setStage("active");
        item.setStatus("PUBLISHED");
        itemMapper.insert(item);
    }

    @Override
    public boolean updateLostItem(LostDTO.UpdateLostReq request) {
        if (request.getId() == null) {
            return false;
        }
        Item item = itemMapper.selectById(request.getId());
        if (item == null || !"lost".equals(item.getScene())) {
            return false;
        }
        BeanUtils.copyProperties(request, item);
        return itemMapper.updateById(item) > 0;
    }

    @Override
    public boolean deleteLostItem(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null || !"lost".equals(item.getScene())) {
            return false;
        }
        return itemMapper.deleteById(id) > 0;
    }

    private LostDTO.LostItemResp convertToDto(Item item) {
        LostDTO.LostItemResp dto = new LostDTO.LostItemResp();
        dto.setId(item.getId());
        dto.setBizId(item.getBizId());
        dto.setItemName(item.getItemName());
        dto.setDescription(item.getDescription());
        dto.setLocation(item.getLocation());
        dto.setTimeLabel(item.getTimeLabel());
        dto.setStatus(item.getStatus());
        dto.setCreateTime(item.getCreateTime());
        return dto;
    }

    private Item convertToEntity(LostDTO.CreateLostReq request) {
        Item item = new Item();
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setTimeLabel(request.getTimeLabel());
        return item;
    }
}