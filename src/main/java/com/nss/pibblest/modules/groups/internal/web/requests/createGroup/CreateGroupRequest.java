package com.nss.pibblest.modules.groups.internal.web.requests.createGroup;

public class CreateGroupRequest {
    
    private String name;

    public CreateGroupRequest(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
