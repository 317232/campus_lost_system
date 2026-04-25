package com.campus.lostfound.catalog.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.catalog.dto.CategoryDTO;
import com.campus.lostfound.catalog.service.CategoryService;
import com.campus.lostfound.domain.entity.Category;
import com.campus.lostfound.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryDTO.CategoryResp> getAllCategories() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        return categories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO.CategoryResp getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return null;
        }
        return convertToDto(category);
    }

    @Override
    public void createCategory(CategoryDTO.CreateCategoryReq request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : "ENABLED");
        categoryMapper.insert(category);
    }

    @Override
    public boolean updateCategory(CategoryDTO.UpdateCategoryReq request) {
        if (request.getId() == null) {
            return false;
        }
        Category category = categoryMapper.selectById(request.getId());
        if (category == null) {
            return false;
        }
        category.setName(request.getName());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean deleteCategory(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }

    private CategoryDTO.CategoryResp convertToDto(Category category) {
        CategoryDTO.CategoryResp dto = new CategoryDTO.CategoryResp();
        dto.setId(category.getId());
        dto.setBizId(category.getBizId());
        dto.setName(category.getName());
        dto.setSortOrder(category.getSortOrder());
        dto.setStatus(category.getStatus());
        dto.setCreateTime(category.getCreatedAt());
        return dto;
    }
}