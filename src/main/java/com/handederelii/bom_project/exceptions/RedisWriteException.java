package com.handederelii.bom_project.exceptions;

public class RedisWriteException extends RuntimeException {
    private final String key;

    public RedisWriteException(String message, String key, Throwable cause) {
        super(message, cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
