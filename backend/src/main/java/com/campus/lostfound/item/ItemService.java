package com.campus.lostfound.item;

import com.campus.lostfound.common.api.PageResponse;
import com.campus.lostfound.item.dto.MatchResp;

public interface ItemService {

    PageResponse<ItemController.ItemResp> getItemsPage(ItemController.ItemQueryReq query);

    ItemController.ItemResp getItemById(Long id);

    void createItem(ItemController.CreateItemReq request);

    boolean updateItem(ItemController.UpdateItemReq request);

    boolean deleteItem(Long id);

    PageResponse<ItemController.ItemResp> getMyItems(Integer page, Integer pageSize, String scene);

    MatchResp getMatches(Long itemId, Integer limit);

    // ── 管理员物品状态变更 ───────────────────────────────────────
    /** 审核通过 */
    void approveItem(Long itemId, Long auditorId);

    /** 审核拒绝 */
    void rejectItem(Long itemId, Long auditorId);

    /** 标记为已认领 */
    void markAsClaimed(Long itemId);

    /** 标记为已找回 */
    void markAsFoundBack(Long itemId);

    /** 设置为下线 */
    void setOffline(Long itemId);

    /** 管理员删除物品 */
    void adminDeleteItem(Long itemId);

    /** 物品主人或管理员更新物品业务状态 */
    void updateItemStatus(Long itemId, String status);
}
