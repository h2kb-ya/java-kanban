package io.github.h2kb.exception;

public class ManagerSaveException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Error occurred while manager saving.";

    public ManagerSaveException(Exception e) {
        super(ERROR_MESSAGE, e);
    }
}
