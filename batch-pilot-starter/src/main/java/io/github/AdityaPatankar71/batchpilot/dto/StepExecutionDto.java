package io.github.AdityaPatankar71.batchpilot.dto;

import java.util.List;

/**
 * Step-level execution detail, including read/write/skip/commit counts and any
 * failure stack traces captured by Spring Batch.
 */
public record StepExecutionDto(
        Long id,
        String stepName,
        String status,
        long readCount,
        long writeCount,
        long commitCount,
        long rollbackCount,
        long readSkipCount,
        long writeSkipCount,
        long processSkipCount,
        long filterCount,
        String exitCode,
        String exitDescription,
        String startTime,
        String endTime,
        Long durationMs,
        List<String> failureExceptions) {
}
