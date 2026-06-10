package io.github.AdityaPatankar71.batchpilot.audit;

/**
 * One row of the action audit log: who did what, to which target, when, and the outcome.
 */
public record AuditEventDto(
        String id,
        String eventTime,
        String principal,
        String action,
        String target,
        String result,
        String message) {
}
