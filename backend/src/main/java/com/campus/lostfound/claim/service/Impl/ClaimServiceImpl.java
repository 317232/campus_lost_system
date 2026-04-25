package com.campus.lostfound.claim.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.claim.dto.ClaimDTO;
import com.campus.lostfound.claim.service.ClaimService;
import com.campus.lostfound.domain.entity.Claim;
import com.campus.lostfound.domain.entity.ClaimDraft;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.ClaimDraftMapper;
import com.campus.lostfound.mapper.ClaimMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ClaimServiceImpl implements ClaimService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DRAFT_EXPIRE_HOURS = 24;

    private final ClaimMapper claimMapper;
    private final ClaimDraftMapper claimDraftMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final SecurityUserUtils securityUserUtils;

    public ClaimServiceImpl(ClaimMapper claimMapper,
                            ClaimDraftMapper claimDraftMapper,
                            ItemMapper itemMapper,
                            UserMapper userMapper,
                            SecurityUserUtils securityUserUtils) {
        this.claimMapper = claimMapper;
        this.claimDraftMapper = claimDraftMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    @Transactional
    public ClaimDTO.DraftResp createDraft(ClaimDTO.CreateDraftReq request) {
        Long claimantId = securityUserUtils.getCurrentUserId();
        if (claimantId == null) {
            throw new IllegalArgumentException("请先登录后再进行认领");
        }

        Item item = itemMapper.selectById(request.getItemId());
        if (item == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        if (!"found".equals(item.getScene())) {
            throw new IllegalArgumentException("只有招领信息可以申请认领");
        }
        if (Objects.equals(item.getOwnerId(), claimantId)) {
            throw new IllegalArgumentException("不能认领自己发布的物品");
        }

        // 检查是否已有有效草稿
        ClaimDraft existingDraft = claimDraftMapper.selectOne(new LambdaQueryWrapper<ClaimDraft>()
            .eq(ClaimDraft::getItemId, request.getItemId())
            .eq(ClaimDraft::getClaimantId, claimantId)
            .eq(ClaimDraft::getStatus, "ACTIVE")
            .gt(ClaimDraft::getExpiresAt, LocalDateTime.now()));
        if (existingDraft != null) {
            ClaimDTO.DraftResp resp = new ClaimDTO.DraftResp();
            resp.setDraftToken(existingDraft.getDraftToken());
            resp.setItemId(request.getItemId());
            resp.setExpiresAt(existingDraft.getExpiresAt());
            resp.setMessage("已有有效草稿，请使用该令牌提交正式申请");
            return resp;
        }

        // 创建新草稿
        ClaimDraft draft = new ClaimDraft();
        draft.setDraftToken(UUID.randomUUID().toString().replace("-", ""));
        draft.setItemId(request.getItemId());
        draft.setClaimantId(claimantId);
        draft.setKeywordProof(request.getKeywordProof().trim());
        draft.setStatus("ACTIVE");
        draft.setExpiresAt(LocalDateTime.now().plusHours(DRAFT_EXPIRE_HOURS));
        claimDraftMapper.insert(draft);

        ClaimDTO.DraftResp resp = new ClaimDTO.DraftResp();
        resp.setDraftToken(draft.getDraftToken());
        resp.setItemId(request.getItemId());
        resp.setExpiresAt(draft.getExpiresAt());
        resp.setMessage("草稿创建成功，请在 " + DRAFT_EXPIRE_HOURS + " 小时内提交正式认领申请");
        return resp;
    }

    @Override
    @Transactional
    public void createClaim(ClaimDTO.CreateClaimReq request) {
        Long claimantId = securityUserUtils.getCurrentUserId();
        if (claimantId == null) {
            throw new IllegalArgumentException("请先登录后再提交认领申请");
        }

        Item item = itemMapper.selectById(request.getItemId());
        if (item == null) {
            throw new IllegalArgumentException("认领失败：物品不存在");
        }
        if (!"found".equals(item.getScene())) {
            throw new IllegalArgumentException("认领失败：该信息不是拾物招领记录");
        }
        if (Objects.equals(item.getOwnerId(), claimantId)) {
            throw new IllegalArgumentException("不能认领自己发布的拾物信息");
        }

        boolean alreadyClaimed = claimMapper.exists(new LambdaQueryWrapper<Claim>()
            .eq(Claim::getItemId, request.getItemId())
            .eq(Claim::getClaimantId, claimantId)
            .in(Claim::getStatus, List.of("REVIEW_PENDING", "APPROVED")));

        if (alreadyClaimed) {
            throw new IllegalArgumentException("您已提交过该物品的认领申请，请勿重复提交");
        }

        Claim claim = new Claim();
        claim.setBizId(generateBizId());
        claim.setItemId(request.getItemId());
        claim.setClaimantId(claimantId);
        claim.setFormalProof(request.getProofDescription().trim());
        claim.setContact(request.getContact().trim());
        claim.setStatus("REVIEW_PENDING");
        claimMapper.insert(claim);
    }

    @Override
    public List<ClaimDTO.ClaimResp> getMyClaims() {
        Long claimantId = securityUserUtils.getCurrentUserId();
        if (claimantId == null) {
            throw new IllegalArgumentException("请先登录后再查看认领记录");
        }

        List<Claim> claims = claimMapper.selectList(new LambdaQueryWrapper<Claim>()
            .eq(Claim::getClaimantId, claimantId)
            .orderByDesc(Claim::getCreatedAt));

        User claimant = userMapper.selectById(claimantId);
        String claimantName = claimant == null ? null : claimant.getName();

        // Batch fetch items to avoid N+1
        List<Long> itemIds = claims.stream().map(Claim::getItemId).distinct().collect(Collectors.toList());
        Map<Long, Item> itemMap = itemIds.isEmpty() ? Map.of() : itemMapper.selectBatchIds(itemIds).stream()
            .collect(Collectors.toMap(Item::getId, item -> item));

        return claims.stream().map(claim -> {
            Item item = itemMap.get(claim.getItemId());
            ClaimDTO.ClaimResp resp = new ClaimDTO.ClaimResp();
            resp.setId(claim.getId());
            resp.setBizId(claim.getBizId());
            resp.setItemId(claim.getItemId());
            resp.setItemName(item == null ? null : item.getItemName());
            resp.setClaimantId(claim.getClaimantId());
            resp.setClaimantName(claimantName);
            resp.setProofDescription(claim.getFormalProof());
            resp.setContact(claim.getContact());
            resp.setStatus(claim.getStatus());
            resp.setAuditRemark(claim.getAuditRemark());
            if (claim.getCreatedAt() != null) {
                resp.setCreateTime(claim.getCreatedAt().format(DATE_TIME_FORMATTER));
            }
            return resp;
        }).collect(Collectors.toList());
    }

    private String generateBizId() {
        return "CLM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
