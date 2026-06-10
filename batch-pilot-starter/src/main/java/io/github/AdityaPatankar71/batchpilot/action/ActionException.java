package io.github.AdityaPatankar71.batchpilot.action;

import org.springframework.http.HttpStatus;

/**
 * Translates a Spring Batch operation failure into an HTTP-friendly error with a
 * specific status (e.g. restarting a completed instance becomes 409 Conflict).
 */
public class ActionException extends RuntimeException {

    private final HttpStatus status;

    public ActionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
