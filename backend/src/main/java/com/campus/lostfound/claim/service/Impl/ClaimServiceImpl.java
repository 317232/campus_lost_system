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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

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

        return claims.stream().map(claim -> {
            Item item = itemMapper.selectById(claim.getItemId());
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
