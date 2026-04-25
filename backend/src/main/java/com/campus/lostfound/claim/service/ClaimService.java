package com.campus.lostfound.claim.service;

import com.campus.lostfound.claim.dto.ClaimDTO;
import java.util.List;

public interface ClaimService {

    /**
     * 创建认领草稿（关键词验证阶段）
     * @param request 包含 itemId 和 keywordProof
     * @return 草稿响应（包含 draftToken）
     */
    ClaimDTO.DraftResp createDraft(ClaimDTO.CreateDraftReq request);

    void createClaim(ClaimDTO.CreateClaimReq request);

    List<ClaimDTO.ClaimResp> getMyClaims();
}
