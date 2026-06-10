package io.github.AdityaPatankar71.batchpilot.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a requested job execution id does not exist. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExecutionNotFoundException extends RuntimeException {

    public ExecutionNotFoundException(Long executionId) {
        super("No job execution found with id " + executionId);
    }
}
