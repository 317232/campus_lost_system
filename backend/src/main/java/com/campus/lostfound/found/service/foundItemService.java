package com.campus.lostfound.found.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.found.dto.FoundItem;

public interface foundItemService extends IService<FoundItem>{
  /**
     * 分页且带条件地查询拾物列表
     *
     * @param pageNum  当前页码
     * @param pageSize 每页显示数量
     * @param keyword  搜索关键字（可选）
     * @return 分页结果对象
     */
    Page<FoundItem> getFoundItemsPage(Integer pageNum, Integer pageSize, String keyword);

}
