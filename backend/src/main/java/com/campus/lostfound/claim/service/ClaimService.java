package com.campus.lostfound.claim.service;

import com.campus.lostfound.claim.dto.ClaimDTO;

public interface ClaimService {
    void createClaim(ClaimDTO.CreateClaimReq request);
}