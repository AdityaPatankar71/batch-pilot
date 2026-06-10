package io.github.AdityaPatankar71.batchpilot.action;

/** Small response payloads for the action endpoints. */
public final class ActionResults {

    private ActionResults() {
    }

    public record RestartResult(Long newExecutionId) {
    }

    public record StopResult(boolean stopRequested) {
    }

    public record LaunchResult(Long executionId) {
    }

    /** Which write actions are currently enabled, so the UI can show/hide controls. */
    public record ActionsCapabilities(boolean restart, boolean stop, boolean launch) {
    }
}
