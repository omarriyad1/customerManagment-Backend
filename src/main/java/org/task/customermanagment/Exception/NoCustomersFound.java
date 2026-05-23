package org.task.customermanagment.Exception;

public class NoCustomersFound extends RuntimeException {
    public NoCustomersFound(String message) {
        super(message);
    }
}
