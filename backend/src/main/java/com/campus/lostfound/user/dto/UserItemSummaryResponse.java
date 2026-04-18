package com.campus.lostfound.user.dto;

import java.time.LocalDateTime;

public record UserItemSummaryResponse(
    Long id,
    String bizId,
    String itemName,
    String scene,
    String location,
    String status,
    LocalDateTime createdAt
) {
}
