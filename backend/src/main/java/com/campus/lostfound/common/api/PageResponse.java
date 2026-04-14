package com.campus.lostfound.common.api;

import java.util.List;

public class PageResponse<T> {

    private final List<T> records;
    private final long total;
    private final long page;
    private final long pageSize;

    public PageResponse(List<T> records, long total, long page, long pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PageResponse<T> empty(long page, long pageSize) {
        return new PageResponse<>(List.of(), 0L, page, pageSize);
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }

    public long getPage() {
        return page;
    }

    public long getPageSize() {
        return pageSize;
    }
}
