import { api } from "../index";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

const userApi = {
  /** 获取当前用户资料 */
  getProfile() {
    return api.get("/users/me").then(unwrap);
  },
  /**
   * 更新当前用户资料
   * @param {{ displayName?, phone?, email?, avatarUrl?, bio? }} payload
   */
  updateProfile(payload) {
    return api.put("/users/me", payload).then(unwrap);
  },
  /** 我发布的物品列表（统一 API） */
  getMyItems(page = 1, pageSize = 10) {
    return api
      .get("/items/me", { params: { page, pageSize } })
      .then(unwrap);
  },
  /** 我发布的失物列表 */
  getMyLostItems() {
    return api.get("/items/me", { params: { scene: "lost" } }).then(unwrap);
  },
  /** 我发布的招领列表 */
  getMyFoundItems() {
    return api
      .get("/items/me", { params: { scene: "found" } })
      .then(unwrap);
  },
};

export { userApi };
