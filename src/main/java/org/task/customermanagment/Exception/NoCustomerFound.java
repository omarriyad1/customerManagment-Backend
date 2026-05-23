package org.task.customermanagment.Exception;

public class NoCustomerFound extends RuntimeException {
    public NoCustomerFound(String message) {
        super(message);
    }
}
