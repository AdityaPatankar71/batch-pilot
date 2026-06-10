package io.github.AdityaPatankar71.batchpilot.action;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a write action is invoked but disabled via {@code batch-pilot.actions.*}. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ActionDisabledException extends RuntimeException {

    public ActionDisabledException(String action) {
        super("Action '" + action + "' is disabled");
    }
}
