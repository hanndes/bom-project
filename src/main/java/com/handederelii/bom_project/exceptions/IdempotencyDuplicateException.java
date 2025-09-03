package com.handederelii.bom_project.exceptions;

public class IdempotencyDuplicateException extends RuntimeException {
    public IdempotencyDuplicateException(String message) {
        super(message);
    }
}
