package io.github.AdityaPatankar71.batchpilot.dto;

import java.util.List;

/**
 * Full detail of a single job execution: parameters, every step with its
 * read/write/skip/commit counts and exit status, plus job-level failure
 * stack traces.
 */
public record ExecutionDetailDto(
        Long executionId,
        Long instanceId,
        String jobName,
        String status,
        String exitCode,
        String exitDescription,
        String createTime,
        String startTime,
        String endTime,
        Long durationMs,
        List<JobParameterDto> parameters,
        List<StepExecutionDto> steps,
        List<String> failureExceptions) {
}
