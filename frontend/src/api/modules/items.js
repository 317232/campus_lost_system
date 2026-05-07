import { api } from "../index";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

const itemApi = {
  // ========== 统一物品 API（Phase 1 P0 修复）==========
  /**
   * 获取物品列表
   * @param {string} scene - 场景类型：lost/found
   * @param {object} params - 查询参数：keyword, category, zone, page, pageSize
   */
  getItems(scene, params = {}) {
    return api
      .get("/items", {
        params: { scene, ...params },
      })
      .then(unwrap);
  },

  getLostItems(params = {}) {
    return this.getItems('lost', params);
  },

  getFoundItems(params = {}) {
    return this.getItems('found', params);
  },

  /**
   * 获取物品详情
   * @param {number} id - 物品ID
   */
  getItemDetail(id) {
    return api.get(`/items/${id}`).then(unwrap);
  },

  getLostDetail(id) {
    return this.getItemDetail(id);
  },

  getFoundDetail(id) {
    return this.getItemDetail(id);
  },

  /**
   * 发布物品
   * @param {object} data - 物品信息
   */
  createItem(data) {
    return api.post("/items", data).then(unwrap);
  },

  /**
   * 更新物品
   * @param {number} id - 物品ID
   * @param {object} data - 更新信息
   */
  updateItem(id, data) {
    return api.put(`/items/${id}`, data).then(unwrap);
  },

  /**
   * 删除物品
   * @param {number} id - 物品ID
   */
  deleteItem(id) {
    return api.delete(`/items/${id}`).then(unwrap);
  },

  /**
   * 获取我的发布记录
   * @param {number} page - 页码
   * @param {number} pageSize - 每页数量
   */
  getMyItems(page = 1, pageSize = 10) {
    return api
      .get("/items/me", { params: { page, pageSize } })
      .then(unwrap);
  },

  /**
   * 解锁物品联系方式
   * @param {number} id - 物品ID
   * @param {string} source - 来源：DETAIL_PAGE/LIST_PAGE
   */
  unlockContact(id, source = 'DETAIL_PAGE') {
    return api.get(`/items/${id}/contact`, { params: { source } }).then(unwrap);
  },

  /**
   * 获取智能匹配推荐
   * @param {number} id - 物品ID
   * @param {number} limit - 返回数量，默认3
   */
  getMatches(id, limit = 3) {
    return api.get(`/items/${id}/matches`, { params: { limit } }).then(unwrap);
  },

  /**
   * 更新物品状态（仅物品主人或管理员可调用）
   * @param {number} id - 物品ID
   * @param {string} status - 目标状态：CLAIMED/FOUND_BACK/OFFLINE 等
   */
  updateItemStatus(id, status) {
    return api.put(`/items/${id}/status`, { status }).then(unwrap);
  },

  // ========== 文件上传 ==========
  /**
   * 上传单个文件
   * @param {File} file - 文件对象
   */
  uploadFile(file) {
    const formData = new FormData();
    formData.append("file", file);
    return api
      .post("/files/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then(unwrap);
  },

  /**
   * 上传多个文件
   * @param {File[]} files - 文件数组
   */
  uploadFiles(files) {
    const formData = new FormData();
    files.forEach((file) => formData.append("files", file));
    return api
      .post("/files/uploads", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then(unwrap);
  },
};

export { itemApi };
