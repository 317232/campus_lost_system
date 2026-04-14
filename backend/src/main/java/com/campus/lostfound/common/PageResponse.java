package com.campus.lostfound.common;

import java.util.List;

public record PageResponse<T>(List<T> records, long total, int pageNum, int pageSize) {
}
