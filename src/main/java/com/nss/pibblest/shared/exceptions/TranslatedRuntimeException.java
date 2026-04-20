package com.nss.pibblest.shared.exceptions;

public class TranslatedRuntimeException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public TranslatedRuntimeException (String messageKey, Object... args){
        super(messageKey);
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
