package com.campus.lostfound.claim;

import com.campus.lostfound.claim.dto.ClaimDTO;
import com.campus.lostfound.claim.service.ClaimService;
import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/claims")
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody ClaimDTO.CreateClaimReq request) {
        try {
            claimService.createClaim(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }

    @GetMapping("/users/me/claims")
    public ResponseEntity<ApiResponse<List<ClaimDTO.ClaimResp>>> myClaims() {
        try {
            return ResponseEntity.ok(ApiResponse.success(claimService.getMyClaims()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, exception.getMessage()));
        }
    }
}
