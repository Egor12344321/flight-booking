package com.tbank.grpc.exception;


import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcHandlerAdvice {

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleInvalidArgument(IllegalArgumentException e) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("error-type", Metadata.ASCII_STRING_MARSHALLER),
                "INVALID_ARGUMENT"
        );

        return Status.INVALID_ARGUMENT
                .withDescription("Invalid request: " + e.getMessage())
                .asRuntimeException(metadata);
    }

    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusRuntimeException handleNotFound(ResourceNotFoundException e) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("error-type", Metadata.ASCII_STRING_MARSHALLER),
                "NOT_FOUND"
        );

        return Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asRuntimeException(metadata);
    }

    @GrpcExceptionHandler(ResourceExhaustedException.class)
    public StatusRuntimeException handleResourceExhausted(ResourceExhaustedException e) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("error-type", Metadata.ASCII_STRING_MARSHALLER),
                "RESOURCE_EXHAUSTED"
        );

        return Status.RESOURCE_EXHAUSTED
                .withDescription(e.getMessage())
                .asRuntimeException(metadata);
    }

    @GrpcExceptionHandler(AlreadyExistsException.class)
    public StatusRuntimeException handleAlreadyExists(AlreadyExistsException e) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("error-type", Metadata.ASCII_STRING_MARSHALLER),
                "ALREADY_EXISTS"
        );

        return Status.ALREADY_EXISTS
                .withDescription(e.getMessage())
                .asRuntimeException(metadata);
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleGeneric(Exception e) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("error-type", Metadata.ASCII_STRING_MARSHALLER),
                "INTERNAL"
        );

        return Status.INTERNAL
                .withDescription("Internal server error: " + e.getMessage())
                .asRuntimeException(metadata);
    }

}
