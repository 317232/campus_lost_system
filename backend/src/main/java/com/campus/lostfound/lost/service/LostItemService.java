package com.campus.lostfound.lost.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.lost.dto.LostDTO;

public interface LostItemService {
    Page<LostDTO.LostItemResp> getLostItemsPage(Integer pageNum, Integer pageSize, String keyword);

    LostDTO.LostItemResp getLostItemById(Long id);

    void createLostItem(LostDTO.CreateLostReq request);

    boolean updateLostItem(LostDTO.UpdateLostReq request);

    boolean deleteLostItem(Long id);
}