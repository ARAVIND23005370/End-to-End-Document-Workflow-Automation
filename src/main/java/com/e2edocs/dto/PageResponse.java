package com.e2edocs.dto;

import java.util.Collections;
import java.util.List;

public class PageResponse<T> {
    private List<T> data;
    private long total;

    public PageResponse() {
        this.data = Collections.emptyList();
        this.total = 0;
    }

    public PageResponse(List<T> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
