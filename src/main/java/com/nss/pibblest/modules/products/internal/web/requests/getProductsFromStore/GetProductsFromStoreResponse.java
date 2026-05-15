package com.nss.pibblest.modules.products.internal.web.requests.getProductsFromStore;

import java.util.List;

import org.springframework.data.domain.Page;

import com.nss.pibblest.modules.products.api.ProductPreviewDto;

public class GetProductsFromStoreResponse {
    private List<ProductPreviewDto> products;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    public GetProductsFromStoreResponse(Page<ProductPreviewDto> productsPage){
        this.products = productsPage.getContent();

        this.pageNo = productsPage.getNumber();
        this.pageSize = productsPage.getSize();
        this.totalElements = productsPage.getTotalElements();
        this.totalPages = productsPage.getTotalPages();
        this.last = productsPage.isLast();

    }
    public List<ProductPreviewDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductPreviewDto> products) {
        this.products = products;
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
