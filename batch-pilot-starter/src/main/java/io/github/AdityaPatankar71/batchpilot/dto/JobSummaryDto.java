package io.github.AdityaPatankar71.batchpilot.dto;

/**
 * Overview row for a single registered job: how many instances it has and a
 * summary of its most recent execution (null if it has never run).
 */
public record JobSummaryDto(String name, long instanceCount, ExecutionSummaryDto lastExecution) {
}
