package com.nss.pibblest.modules.tags.internal.core.exceptions;

public class TagsNotFound extends RuntimeException {
    private String message;


    public TagsNotFound (String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
