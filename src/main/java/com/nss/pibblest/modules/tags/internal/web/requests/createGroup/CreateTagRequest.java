package com.nss.pibblest.modules.tags.internal.web.requests.createGroup;

public class CreateTagRequest {
    
    private String name;

    public CreateTagRequest(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
