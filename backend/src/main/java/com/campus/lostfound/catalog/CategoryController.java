package com.campus.lostfound.catalog;

import com.campus.lostfound.catalog.dto.CategoryDTO;
import com.campus.lostfound.catalog.service.CategoryService;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO.CategoryResp>>> list() {
        List<CategoryDTO.CategoryResp> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO.CategoryResp>> detail(@PathVariable Long id) {
        CategoryDTO.CategoryResp category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ResultCode.NOT_FOUND, "分类不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody CategoryDTO.CreateCategoryReq request) {
        categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO.UpdateCategoryReq request) {
        request.setId(id);
        boolean updated = categoryService.updateCategory(request);
        if (updated) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "更新失败，分类可能不存在"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure(ResultCode.NOT_FOUND, "删除失败，分类可能不存在"));
    }
}