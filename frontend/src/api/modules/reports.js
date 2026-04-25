import { api } from "../index";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

const reportApi = {
  /**
   * 提交举报
   * @param {object} data - { targetType, targetId, reason, detail }
   */
  createReport(data) {
    return api.post("/reports", data).then(unwrap);
  },

  /**
   * 获取我的举报记录
   */
  getMyReports(page = 1, pageSize = 10) {
    return api.get("/reports", { params: { pageNum: page, pageSize } }).then(unwrap);
  },

  /**
   * 获取举报列表（管理端）
   */
  getReports(params = {}) {
    return api.get("/reports", { params }).then(unwrap);
  },

  /**
   * 获取举报详情
   * @param {number} id - 举报ID
   */
  getReportDetail(id) {
    return api.get(`/api/reports/${id}`).then(unwrap);
  },
};

export { reportApi };