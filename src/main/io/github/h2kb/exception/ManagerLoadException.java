package io.github.h2kb.exception;

public class ManagerLoadException extends RuntimeException {

    public ManagerLoadException(String message, Exception e) {
        super(message, e);
    }

}
