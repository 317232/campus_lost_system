package com.campus.lostfound.found.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.found.dto.FoundItem;

public interface FoundItemService {
    /**
   * 分页且带条件地查询拾物列表
   *
   * @param pageNum  当前页码
   * @param pageSize 每页显示数量
   * @param keyword  搜索关键字（可选）
   * @return 分页结果对象
   */
    Page<FoundItem> getFoundItemsPage(Integer pageNum, Integer pageSize, String keyword);

    /**
   * 根据 ID 获取拾物详情 (返回 DTO)
   */
    FoundItem getFoundItemById(Long id);

    /**
   * 创建拾物信息 (接收 DTO，转换为 Entity 插入)
   */
    boolean createFoundItem(FoundItem foundItemDto);

    /**
   * 更新拾物信息 (接收 DTO，转换为 Entity 更新)
   */
    boolean updateFoundItem(FoundItem foundItemDto);

      /**
   * 删除拾物信息 (根据 ID 删除 Entity)
   */
    boolean deleteFoundItem(Long id);
}