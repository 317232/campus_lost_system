package com.campus.lostfound.claim.service;

import com.campus.lostfound.claim.dto.ClaimDTO;
import java.util.List;

public interface ClaimService {
    void createClaim(ClaimDTO.CreateClaimReq request);

    List<ClaimDTO.ClaimResp> getMyClaims();
}
