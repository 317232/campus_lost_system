package com.campus.lostfound.claim;

import com.campus.lostfound.common.ApiResponse;
import com.campus.lostfound.demo.DemoDataService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClaimController {

    private final DemoDataService demoDataService;

    public ClaimController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @PostMapping("/api/claims")
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success("claim-created", payload);
    }

    @GetMapping("/api/users/me/claims")
    public ApiResponse<?> myClaims() {
        return ApiResponse.success(demoDataService.claims());
    }
}
