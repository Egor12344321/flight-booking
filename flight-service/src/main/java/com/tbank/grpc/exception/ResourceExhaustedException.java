package com.tbank.grpc.exception;

public class ResourceExhaustedException extends RuntimeException {

    public ResourceExhaustedException(String message) {
        super(message);
    }
}