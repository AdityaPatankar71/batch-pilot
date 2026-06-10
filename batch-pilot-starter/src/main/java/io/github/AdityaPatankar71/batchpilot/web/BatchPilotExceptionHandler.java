package io.github.AdityaPatankar71.batchpilot.web;

import io.github.AdityaPatankar71.batchpilot.action.ActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps batch-pilot action failures to RFC-7807 problem responses. Scoped to the
 * console controllers so it never interferes with the host application's own
 * exception handling.
 */
@RestControllerAdvice(assignableTypes = {BatchPilotController.class, BatchPilotActionController.class})
public class BatchPilotExceptionHandler {

    @ExceptionHandler(ActionException.class)
    public ProblemDetail handleActionException(ActionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle("Batch action failed");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }
}
