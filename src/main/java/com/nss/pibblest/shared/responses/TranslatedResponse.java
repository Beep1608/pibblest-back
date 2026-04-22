package com.nss.pibblest.shared.responses;

public class TranslatedResponse {
    private final String messageKey;
    private final Object[] args;

    public TranslatedResponse (String messageKey, Object... args){
        this.messageKey = messageKey;
        this.args = args;

    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
