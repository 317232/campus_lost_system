package com.campus.lostfound.lost;

import com.campus.lostfound.common.ApiResponse;
import com.campus.lostfound.demo.DemoDataService;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final DemoDataService demoDataService;

    public LostItemController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(demoDataService.lostItems());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(demoDataService.lostItems().records().stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElse(demoDataService.lostItems().records().get(0)));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success("lost-item-created", payload);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success("lost-item-updated", Map.of("id", id, "payload", payload));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return ApiResponse.success("lost-item-deleted", Map.of("id", id));
    }
}
