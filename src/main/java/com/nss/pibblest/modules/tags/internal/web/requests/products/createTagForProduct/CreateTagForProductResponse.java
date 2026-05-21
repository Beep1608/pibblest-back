package com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct;

public class CreateTagForProductResponse {
    private String message;

    public CreateTagForProductResponse(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }


}
