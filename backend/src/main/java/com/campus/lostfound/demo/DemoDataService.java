package com.campus.lostfound.demo;

import com.campus.lostfound.common.PageResponse;
import com.campus.lostfound.common.enums.AuditStatus;
import com.campus.lostfound.common.enums.ClaimStatus;
import com.campus.lostfound.common.enums.FoundItemStatus;
import com.campus.lostfound.common.enums.LostItemStatus;
import com.campus.lostfound.common.enums.ReportStatus;
import com.campus.lostfound.common.enums.UserRole;
import com.campus.lostfound.demo.DemoDtos.AuthToken;
import com.campus.lostfound.demo.DemoDtos.ClaimRecord;
import com.campus.lostfound.demo.DemoDtos.FoundDraft;
import com.campus.lostfound.demo.DemoDtos.ItemCard;
import com.campus.lostfound.demo.DemoDtos.LostDraft;
import com.campus.lostfound.demo.DemoDtos.NoticeRecord;
import com.campus.lostfound.demo.DemoDtos.OverviewMetric;
import com.campus.lostfound.demo.DemoDtos.ReportRecord;
import com.campus.lostfound.demo.DemoDtos.ReviewTask;
import com.campus.lostfound.demo.DemoDtos.TrendPoint;
import com.campus.lostfound.demo.DemoDtos.UserProfile;
import com.campus.lostfound.security.JwtTokenProvider;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DemoDataService {

    private final JwtTokenProvider jwtTokenProvider;

    public DemoDataService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthToken loginToken(String account) {
        UserRole role = account != null && account.startsWith("admin") ? UserRole.ADMIN : UserRole.USER;
        Long userId = role == UserRole.ADMIN ? 2L : 1L;
        String username = role == UserRole.ADMIN ? "admin" : "user";
        String displayName = role == UserRole.ADMIN ? "管理员" : "普通用户";
        String token = jwtTokenProvider.createToken(userId, username, role.name());
        return new AuthToken(token, role, displayName);
    }

    public UserProfile currentUser() {
        return new UserProfile(1L, "2023123456", "王同学", "2023123456", "18812341024",
            "demo@campus.edu.cn", UserRole.USER);
    }

    public List<String> categories() {
        return List.of("证件", "电子产品", "书籍资料", "钥匙卡证", "衣物配饰", "其他");
    }

    public PageResponse<ItemCard> lostItems() {
        return new PageResponse<>(
            List.of(
                new ItemCard(101L, "黑色双肩包丢失", "双肩包", "书籍资料", "图书馆二层自习区",
                    "2026-04-11 15:30", "包内有数据库原理教材和校园卡套。", "188****1024", "王同学",
                    AuditStatus.APPROVED, "PUBLISHED"),
                new ItemCard(102L, "AirPods 充电盒丢失", "AirPods 充电盒", "电子产品", "食堂一楼",
                    "2026-04-10 12:20", "透明保护套，盒盖内侧有蓝色贴纸。", "136****9812", "赵同学",
                    AuditStatus.PENDING, "PENDING_REVIEW")
            ), 2, 1, 10);
    }

    public PageResponse<ItemCard> foundItems() {
        return new PageResponse<>(
            List.of(
                new ItemCard(201L, "教学楼捡到学生证", "学生证", "证件", "A3 教学楼 301 门口",
                    "2026-04-12 08:45", "证件套为透明磨砂款。", "校青协值班号", "刘老师",
                    AuditStatus.APPROVED, "WAITING_CLAIM"),
                new ItemCard(202L, "操场看台拾到车钥匙", "车钥匙", "钥匙卡证", "操场东侧看台",
                    "2026-04-11 18:05", "黑色钥匙扣，挂有小熊玩偶。", "137****2213", "陈同学",
                    AuditStatus.APPROVED, "CLAIMED")
            ), 2, 1, 10);
    }

    public List<ClaimRecord> claims() {
        return List.of(
            new ClaimRecord(301L, "教学楼捡到学生证", "补充了证件号码后四位和学院信息", ClaimStatus.PENDING, "2026-04-12 11:20"),
            new ClaimRecord(302L, "操场看台拾到车钥匙", "描述了钥匙扣上的小熊挂件", ClaimStatus.APPROVED, "2026-04-11 21:10")
        );
    }

    public List<NoticeRecord> notices() {
        return List.of(
            new NoticeRecord(1L, "值班室认领提醒", "工作日 9:00 - 17:30 可携带有效证明前往值班室认领。", "2026-04-10"),
            new NoticeRecord(2L, "证件类物品优先登记", "身份证、学生证、银行卡类物品将在审核通过后优先展示。", "2026-04-08")
        );
    }

    public List<ReviewTask> reviewTasks() {
        return List.of(
            new ReviewTask("L-208", "图书馆丢失笔记本电脑", "赵同学", "失物待审", AuditStatus.PENDING),
            new ReviewTask("F-116", "食堂捡到校园卡", "后勤值班", "招领待审", AuditStatus.PENDING),
            new ReviewTask("C-046", "学生证认领申请", "李同学", "认领待审", AuditStatus.PENDING)
        );
    }

    public List<ReportRecord> reports() {
        return List.of(
            new ReportRecord(18L, "重复发布疑似广告内容", "found_item#41", ReportStatus.PENDING),
            new ReportRecord(19L, "联系方式疑似无效", "lost_item#66", ReportStatus.PENDING)
        );
    }

    public List<UserProfile> adminUsers() {
        return List.of(
            new UserProfile(1L, "2023123456", "王同学", "2023123456", "18812341024",
                "demo@campus.edu.cn", UserRole.USER),
            new UserProfile(2L, "admin01", "系统管理员", "A0001", "18800000000",
                "admin@campus.edu.cn", UserRole.ADMIN)
        );
    }

    public List<OverviewMetric> statisticsOverview() {
        return List.of(
            new OverviewMetric("totalUsers", 128),
            new OverviewMetric("totalLostPosts", 56),
            new OverviewMetric("totalFoundPosts", 38),
            new OverviewMetric("pendingLostReviews", 8),
            new OverviewMetric("pendingFoundReviews", 5),
            new OverviewMetric("pendingClaimReviews", 3),
            new OverviewMetric("pendingReports", 2),
            new OverviewMetric("completedClaims", 26)
        );
    }

    public List<TrendPoint> statisticsTrend() {
        return List.of(
            new TrendPoint("04-06", 6),
            new TrendPoint("04-07", 9),
            new TrendPoint("04-08", 7),
            new TrendPoint("04-09", 12),
            new TrendPoint("04-10", 8),
            new TrendPoint("04-11", 14),
            new TrendPoint("04-12", 10)
        );
    }

    public List<LostDraft> myLostDrafts() {
        return List.of(
            new LostDraft(101L, "黑色双肩包丢失", LostItemStatus.PUBLISHED),
            new LostDraft(102L, "AirPods 充电盒丢失", LostItemStatus.PENDING_REVIEW)
        );
    }

    public List<FoundDraft> myFoundDrafts() {
        return List.of(
            new FoundDraft(201L, "教学楼捡到学生证", FoundItemStatus.WAITING_CLAIM),
            new FoundDraft(202L, "操场看台拾到车钥匙", FoundItemStatus.CLAIMED)
        );
    }
}
