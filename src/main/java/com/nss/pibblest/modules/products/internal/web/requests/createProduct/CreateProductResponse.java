package com.nss.pibblest.modules.products.internal.web.requests.createProduct;


public class CreateProductResponse {
    
    private Long id;
    private String message;
    public CreateProductResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
