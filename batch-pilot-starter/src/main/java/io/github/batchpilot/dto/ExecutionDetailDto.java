/*
 * Copyright 2026 Aditya Patankar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.batchpilot.dto;

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
