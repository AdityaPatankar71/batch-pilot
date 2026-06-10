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
package io.github.AdityaPatankar71.batchpilot.web;

import io.github.AdityaPatankar71.batchpilot.dto.ExecutionDetailDto;
import io.github.AdityaPatankar71.batchpilot.dto.ExecutionSummaryDto;
import io.github.AdityaPatankar71.batchpilot.dto.JobSummaryDto;
import io.github.AdityaPatankar71.batchpilot.service.JobQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only REST API backing the batch-pilot console.
 *
 * <p>M0 scope: list jobs, list executions per job, and execution detail. No
 * restart/launch/stop actions are exposed yet.
 */
@RestController
@RequestMapping("/batch-pilot/api")
public class BatchPilotController {

    private final JobQueryService jobQueryService;

    public BatchPilotController(JobQueryService jobQueryService) {
        this.jobQueryService = jobQueryService;
    }

    /** Every registered job with its last-execution summary. */
    @GetMapping("/jobs")
    public List<JobSummaryDto> listJobs() {
        return jobQueryService.listJobs();
    }

    /** Executions for a single job, most recent first. */
    @GetMapping("/jobs/{jobName}/executions")
    public List<ExecutionSummaryDto> listExecutions(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jobQueryService.listExecutions(jobName, page, size);
    }

    /** Full detail for one execution, including step counts and stack traces. */
    @GetMapping("/executions/{executionId}")
    public ExecutionDetailDto getExecution(@PathVariable Long executionId) {
        return jobQueryService.getExecutionDetail(executionId);
    }
}
