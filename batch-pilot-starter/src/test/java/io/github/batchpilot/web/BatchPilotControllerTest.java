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
package io.github.batchpilot.web;

import io.github.batchpilot.dto.ExecutionDetailDto;
import io.github.batchpilot.dto.ExecutionSummaryDto;
import io.github.batchpilot.dto.JobParameterDto;
import io.github.batchpilot.dto.JobSummaryDto;
import io.github.batchpilot.dto.StepExecutionDto;
import io.github.batchpilot.service.ExecutionNotFoundException;
import io.github.batchpilot.service.JobQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives {@link BatchPilotController} through a standalone MockMvc, so no Spring
 * context (and therefore no @SpringBootConfiguration) is required for this
 * library module. The query service is mocked.
 */
class BatchPilotControllerTest {

    private final JobQueryService jobQueryService = mock(JobQueryService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BatchPilotController(jobQueryService)).build();
    }

    @Test
    void listJobs_returnsJobSummaries() throws Exception {
        ExecutionSummaryDto last = new ExecutionSummaryDto(
                99L, 42L, "dailyJob", "COMPLETED", "COMPLETED",
                "2026-06-10T09:00:00", "2026-06-10T09:00:01", "2026-06-10T09:00:03",
                2000L, List.of(new JobParameterDto("inputFile", "feed.csv", "java.lang.String", true)));
        when(jobQueryService.listJobs())
                .thenReturn(List.of(new JobSummaryDto("dailyJob", 3L, last)));

        mockMvc.perform(get("/batch-pilot/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("dailyJob"))
                .andExpect(jsonPath("$[0].instanceCount").value(3))
                .andExpect(jsonPath("$[0].lastExecution.status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].lastExecution.durationMs").value(2000))
                .andExpect(jsonPath("$[0].lastExecution.parameters[0].name").value("inputFile"));
    }

    @Test
    void listExecutions_returnsExecutionSummaries() throws Exception {
        ExecutionSummaryDto e = new ExecutionSummaryDto(
                100L, 42L, "dailyJob", "FAILED", "FAILED",
                "2026-06-10T09:00:00", "2026-06-10T09:00:01", "2026-06-10T09:00:02",
                1000L, List.of());
        when(jobQueryService.listExecutions("dailyJob", 0, 20)).thenReturn(List.of(e));

        mockMvc.perform(get("/batch-pilot/api/jobs/dailyJob/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].executionId").value(100))
                .andExpect(jsonPath("$[0].status").value("FAILED"));
    }

    @Test
    void getExecution_returnsDetailWithSteps() throws Exception {
        StepExecutionDto step = new StepExecutionDto(
                7L, "processStep", "FAILED", 100, 80, 8, 1, 2, 3, 4, 5,
                "FAILED", "step failed", "2026-06-10T09:00:01", "2026-06-10T09:00:02",
                1000L, List.of("java.lang.RuntimeException: kaboom"));
        ExecutionDetailDto detail = new ExecutionDetailDto(
                99L, 42L, "dailyJob", "FAILED", "FAILED", "job blew up",
                "2026-06-10T09:00:00", "2026-06-10T09:00:01", "2026-06-10T09:00:02",
                1000L, List.of(), List.of(step), List.of("java.lang.IllegalStateException: boom"));
        when(jobQueryService.getExecutionDetail(99L)).thenReturn(detail);

        mockMvc.perform(get("/batch-pilot/api/executions/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(99))
                .andExpect(jsonPath("$.steps[0].stepName").value("processStep"))
                .andExpect(jsonPath("$.steps[0].readCount").value(100))
                .andExpect(jsonPath("$.steps[0].failureExceptions[0]").value("java.lang.RuntimeException: kaboom"))
                .andExpect(jsonPath("$.failureExceptions[0]").value("java.lang.IllegalStateException: boom"));
    }

    @Test
    void getExecution_unknownId_returns404() throws Exception {
        when(jobQueryService.getExecutionDetail(404L)).thenThrow(new ExecutionNotFoundException(404L));

        mockMvc.perform(get("/batch-pilot/api/executions/404"))
                .andExpect(status().isNotFound());
    }
}
