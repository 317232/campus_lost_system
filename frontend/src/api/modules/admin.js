import { api } from "../index";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

const adminApi = {
  // ── 数据统计 ──────────────────────────────────────────────────
  getOverview() {
    return api.get("/admin/dashboard").then(unwrap);
  },
  getTrend() {
    return api.get("/admin/statistics/trend").then(unwrap);
  },
  getReviewQueue() {
    return api.get("/admin/items/review").then(unwrap);
  },

  // ── 物品审核 ──────────────────────────────────────────────────
  auditItem(itemId, action, remark = '') {
    // action: 'APPROVE' | 'REJECT' → backend expects 'APPROVED' | 'REJECTED'
    const auditStatus = action === 'APPROVE' ? 'APPROVED' : 'REJECTED'
    return api.post('/admin/items/audit', { itemId, auditStatus, auditRemark: remark }).then(unwrap)
  },

  // ── 认领审核 ──────────────────────────────────────────────────
  auditClaim(claimId, action, remark = '') {
    const auditStatus = action === 'APPROVE' ? 'APPROVED' : 'REJECTED'
    return api.post('/admin/claims/audit', { claimId, auditStatus, auditRemark: remark }).then(unwrap)
  },
  getClaimReviewQueue(params) {
    return api.get('/admin/claims/review', { params }).then(unwrap);
  },
  getClaimAuditDetail(claimId) {
    return api.get(`/admin/claims/${claimId}`).then(unwrap);
  },

  // ── 用户管理 ──────────────────────────────────────────────────
  getUsers() {
    return api.get("/admin/users").then(unwrap);
  },
  createUser(payload) {
    return api.post("/admin/users", payload).then(unwrap);
  },
  updateUser(userId, payload) {
    return api.put(`/admin/users/${userId}`, payload).then(unwrap);
  },
  deleteUser(userId) {
    return api.delete(`/admin/users/${userId}`).then(unwrap);
  },
  updateUserStatus(userId, status) {
    return api.put(`/admin/users/${userId}/status`, { status }).then(unwrap);
  },

  // ── 公告管理 ──────────────────────────────────────────────────
  getNotices() {
    return api.get("/admin/notices").then(unwrap);
  },
  createNotice(payload) {
    return api.post("/admin/notices", payload).then(unwrap);
  },
  updateNotice(id, payload) {
    return api.put(`/admin/notices/${id}`, payload).then(unwrap);
  },
  deleteNotice(id) {
    return api.delete(`/admin/notices/${id}`).then(unwrap);
  },

  // ── 物品分类 ──────────────────────────────────────────────────
  getCategories() {
    return api.get("/admin/categories").then(unwrap);
  },
  createCategory(payload) {
    return api.post("/admin/categories", payload).then(unwrap);
  },
  updateCategory(id, payload) {
    return api.put(`/admin/categories/${id}`, payload).then(unwrap);
  },
  deleteCategory(id) {
    return api.delete(`/admin/categories/${id}`).then(unwrap);
  },

  // ── 举报处理 ──────────────────────────────────────────────────
  getReports(params) {
    return api.get("/admin/reports", { params }).then(unwrap);
  },
  handleReport(reportId, status, handleRemark = '') {
    return api.post('/admin/reports/handle', { reportId, status, handleRemark }).then(unwrap);
  },
  deleteReport(id) {
    return api.delete(`/admin/reports/${id}`).then(unwrap);
  },
};

export { adminApi };
