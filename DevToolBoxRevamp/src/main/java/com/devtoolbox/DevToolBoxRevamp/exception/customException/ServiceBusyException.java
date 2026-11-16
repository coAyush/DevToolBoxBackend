package com.devtoolbox.DevToolBoxRevamp.exception.customException;

public class ServiceBusyException extends RuntimeException {
    public ServiceBusyException(String message) {
        super(message);
    }
}
