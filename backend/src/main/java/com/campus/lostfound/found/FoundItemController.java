package com.campus.lostfound.found;

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
@RequestMapping("/api/found-items")
public class FoundItemController {

    private final DemoDataService demoDataService;

    public FoundItemController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(demoDataService.foundItems());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(demoDataService.foundItems().records().stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElse(demoDataService.foundItems().records().get(0)));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success("found-item-created", payload);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success("found-item-updated", Map.of("id", id, "payload", payload));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return ApiResponse.success("found-item-deleted", Map.of("id", id));
    }
}
