package io.github.h2kb.exception;

public class ManagerLoadException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Error occurred while manager loading.";

    public ManagerLoadException(Exception e) {
        super(ERROR_MESSAGE, e);
    }

}
