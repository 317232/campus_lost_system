package com.campus.lostfound.catalog.service;

import com.campus.lostfound.catalog.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    List<CategoryDTO.CategoryResp> getAllCategories();

    CategoryDTO.CategoryResp getCategoryById(Long id);

    void createCategory(CategoryDTO.CreateCategoryReq request);

    boolean updateCategory(CategoryDTO.UpdateCategoryReq request);

    boolean deleteCategory(Long id);
}