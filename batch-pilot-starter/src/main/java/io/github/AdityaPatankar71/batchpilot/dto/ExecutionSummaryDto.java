package io.github.AdityaPatankar71.batchpilot.dto;

import java.util.List;

/**
 * Lightweight summary of a single job execution, used in list views.
 */
public record ExecutionSummaryDto(
        Long executionId,
        Long instanceId,
        String jobName,
        String status,
        String exitCode,
        String createTime,
        String startTime,
        String endTime,
        Long durationMs,
        List<JobParameterDto> parameters) {
}
