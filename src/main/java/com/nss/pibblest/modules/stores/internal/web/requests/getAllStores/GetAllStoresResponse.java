package com.nss.pibblest.modules.stores.internal.web.requests.getAllStores;

import java.util.List;

import org.springframework.data.domain.Page;

import com.nss.pibblest.modules.stores.api.dtos.StoreDto;

public class GetAllStoresResponse {

    private List<StoreDto> stores;

    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public GetAllStoresResponse(Page<StoreDto> page) {
        this.stores = page.getContent();
        this.pageNo = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }

    public List<StoreDto> getStores() {
        return stores;
    }

    public void setStores(List<StoreDto> stores) {
        this.stores = stores;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

}
