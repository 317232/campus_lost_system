package com.campus.lostfound.claim.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.claim.dto.ClaimDTO;
import com.campus.lostfound.claim.service.ClaimService;
import com.campus.lostfound.domain.entity.Claim;
import com.campus.lostfound.domain.entity.Item;
import com.campus.lostfound.domain.entity.User;
import com.campus.lostfound.mapper.ClaimMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimServiceImpl implements ClaimService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ClaimMapper claimMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final SecurityUserUtils securityUserUtils;

    public ClaimServiceImpl(ClaimMapper claimMapper,
                            ItemMapper itemMapper,
                            UserMapper userMapper,
                            SecurityUserUtils securityUserUtils) {
        this.claimMapper = claimMapper;
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
        if (!"FOUND".equals(item.getScene())) {
            throw new IllegalArgumentException("只有招领信息可以申请认领");
        }
        if (!"PUBLISHED".equals(item.getStatus())) {
            throw new IllegalArgumentException("该物品当前状态不允许认领");
        }
        if (Objects.equals(item.getOwnerId(), claimantId)) {
            throw new IllegalArgumentException("不能认领自己发布的物品");
        }

        ClaimDTO.DraftResp resp = new ClaimDTO.DraftResp();
        resp.setItemId(request.getItemId());
        resp.setMessage("可直接提交认领申请");
        return resp;
    }

    @Override
    @Transactional
    public void createClaim(ClaimDTO.CreateClaimReq request) {
        Long claimantId = securityUserUtils.getCurrentUserId();
        if (claimantId == null) {
            throw new IllegalArgumentException("请先登录后再提交认领申请");
        }

        doCreateClaim(request.getItemId(), claimantId,
                request.getProofDescription(),
                request.getKeywordProof(),
                request.getContact());
    }

    @Override
    @Transactional
    public void applyClaim(ClaimDTO.ClaimApplyReq request) {
        Long claimantId = securityUserUtils.getCurrentUserId();
        if (claimantId == null) {
            throw new IllegalArgumentException("请先登录后再提交认领申请");
        }

        doCreateClaim(request.getItemId(), claimantId,
                request.getClaimDescription(),
                request.getProofMaterial(),
                request.getContact());
    }

    /**
     * 统一的认领创建逻辑
     */
    private void doCreateClaim(Long itemId, Long claimantId,
                               String claimStatement, String featureProof, String contact) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("认领失败：物品不存在");
        }
        if (!"FOUND".equals(item.getScene())) {
            throw new IllegalArgumentException("认领失败：该信息不是拾物招领记录");
        }
        if (!"PUBLISHED".equals(item.getStatus())) {
            throw new IllegalArgumentException("认领失败：该物品当前状态不允许认领");
        }
        if (Objects.equals(item.getOwnerId(), claimantId)) {
            throw new IllegalArgumentException("不能认领自己发布的拾物信息");
        }

        boolean alreadyClaimed = claimMapper.exists(new LambdaQueryWrapper<Claim>()
            .eq(Claim::getItemId, itemId)
            .eq(Claim::getClaimantId, claimantId)
            .in(Claim::getStatus, List.of("APPLIED", "REVIEW_PENDING", "APPROVED")));

        if (alreadyClaimed) {
            throw new IllegalArgumentException("您已提交过该物品的认领申请，请勿重复提交");
        }

        Claim claim = new Claim();
        claim.setBizId(generateBizId());
        claim.setItemId(itemId);
        claim.setClaimantId(claimantId);
        claim.setClaimStatement(claimStatement != null ? claimStatement.trim() : null);
        claim.setFeatureProof(featureProof != null ? featureProof.trim() : null);
        claim.setContact(contact != null ? contact.trim() : null);
        claim.setStatus("APPLIED");
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
            .orderByDesc(Claim::getCreateTime));

        User claimant = userMapper.selectById(claimantId);
        String claimantName = claimant == null ? null : claimant.getName();

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
            resp.setProofDescription(claim.getClaimStatement());
            resp.setContact(claim.getContact());
            resp.setStatus(claim.getStatus());
            resp.setAuditRemark(claim.getAuditRemark());
            if (claim.getCreateTime() != null) {
                resp.setCreateTime(claim.getCreateTime().format(DATE_TIME_FORMATTER));
            }
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ClaimDTO.MyClaimVO> getMyClaimApplications() {
        Long claimantId = securityUserUtils.getCurrentUserId();
        if (claimantId == null) {
            throw new IllegalArgumentException("请先登录后再查看认领记录");
        }

        List<Claim> claims = claimMapper.selectList(new LambdaQueryWrapper<Claim>()
            .eq(Claim::getClaimantId, claimantId)
            .orderByDesc(Claim::getCreateTime));

        List<Long> itemIds = claims.stream().map(Claim::getItemId).distinct().collect(Collectors.toList());
        Map<Long, Item> itemMap = itemIds.isEmpty() ? Map.of() :
            itemMapper.selectBatchIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        return claims.stream().map(claim -> {
            Item item = itemMap.get(claim.getItemId());
            ClaimDTO.MyClaimVO vo = new ClaimDTO.MyClaimVO();
            vo.setId(claim.getId());
            vo.setBizId(claim.getBizId());
            vo.setItemId(claim.getItemId());
            vo.setItemName(item != null ? item.getItemName() : null);
            vo.setItemLocation(item != null ? item.getLocation() : null);
            vo.setClaimDescription(claim.getClaimStatement());
            vo.setProofMaterial(claim.getFeatureProof());
            vo.setContact(claim.getContact());
            vo.setStatus(claim.getStatus());
            if (claim.getCreateTime() != null) {
                vo.setCreateTime(claim.getCreateTime().format(DATE_TIME_FORMATTER));
            }
            if (claim.getUpdateTime() != null) {
                vo.setUpdateTime(claim.getUpdateTime().format(DATE_TIME_FORMATTER));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private String generateBizId() {
        return "CLM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}