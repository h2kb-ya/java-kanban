package io.github.h2kb.exception;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message, Exception e) {
        super(message, e);
    }
}
