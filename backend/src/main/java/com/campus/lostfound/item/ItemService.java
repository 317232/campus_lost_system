package com.campus.lostfound.item;

import com.campus.lostfound.common.api.PageResponse;

public interface ItemService {

    PageResponse<ItemController.ItemResp> getItemsPage(ItemController.ItemQueryReq query);

    ItemController.ItemResp getItemById(Long id);

    void createItem(ItemController.CreateItemReq request);

    boolean updateItem(ItemController.UpdateItemReq request);

    boolean deleteItem(Long id);

    PageResponse<ItemController.ItemResp> getMyItems(Integer page, Integer pageSize, String scene);
}