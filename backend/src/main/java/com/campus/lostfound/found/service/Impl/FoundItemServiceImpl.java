package com.campus.lostfound.found.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.found.dto.FoundItem;
import com.campus.lostfound.found.service.FoundItemService;
import com.campus.lostfound.mapper.ItemMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoundItemServiceImpl implements FoundItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public Page<FoundItem> getFoundItemsPage(Integer pageNum, Integer pageSize, String keyword) {
        // 1.创建mybatis-plus分页对象
        Page<Item> page = new Page<>(pageNum, pageSize);
        // 2. 构建查询条件
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getScene, "found");  // 仅查询拾物类型
        
        // 3. 关键字模糊搜索（物品名称 OR 描述）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like(Item::getItemName, keyword)
                    .or()
                    .like(Item::getDescription, keyword));
        }
        
        // 4. 执行分页查询
        Page<Item> itemPage = itemMapper.selectPage(page, queryWrapper);
        
        // 5. Entity → DTO 转换
        Page<FoundItem> dtoPage = new Page<>(
            itemPage.getCurrent(),   // 当前页
            itemPage.getSize(),      // 每页条数
            itemPage.getTotal()      // 总记录数
        );
        
        List<FoundItem> dtoList = itemPage.getRecords()
            .stream()
            .map(this::convertToDto)  // 转换为前端 DTO
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
        Item item = convertToEntity(foundItemDto);
        item.setScene("found");
        item.setStage("active");
        item.setStatus("PUBLISHED");
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
        BeanUtils.copyProperties(foundItemDto, item);
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

    private FoundItem convertToDto(Item item) {
        FoundItem dto = new FoundItem();
        dto.setId(item.getId());
        dto.setItemName(item.getItemName());
        dto.setDescription(item.getDescription());
        dto.setLocation(item.getLocation());
        dto.setContactName(item.getContactVisibility());
        dto.setStatus(item.getStatus());
        dto.setCreateTime(item.getCreateTime());
        return dto;
    }

    private Item convertToEntity(FoundItem dto) {
        Item item = new Item();
        if (dto.getId() != null) {
            item.setId(dto.getId());
        }
        item.setItemName(dto.getItemName());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setContactVisibility(dto.getContactName());
        return item;
    }
}