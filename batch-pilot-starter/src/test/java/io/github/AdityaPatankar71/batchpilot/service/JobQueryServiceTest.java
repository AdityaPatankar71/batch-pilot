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
package io.github.AdityaPatankar71.batchpilot.service;

import io.github.AdityaPatankar71.batchpilot.dto.ExecutionDetailDto;
import io.github.AdityaPatankar71.batchpilot.dto.ExecutionSummaryDto;
import io.github.AdityaPatankar71.batchpilot.dto.JobSummaryDto;
import io.github.AdityaPatankar71.batchpilot.dto.StepExecutionDto;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobQueryServiceTest {

    private final JobExplorer jobExplorer = mock(JobExplorer.class);
    private final JobQueryService service = new JobQueryService(jobExplorer);

    private static final LocalDateTime CREATE = LocalDateTime.of(2026, 6, 10, 9, 0, 0);
    private static final LocalDateTime START = LocalDateTime.of(2026, 6, 10, 9, 0, 1);
    private static final LocalDateTime END = LocalDateTime.of(2026, 6, 10, 9, 0, 2, 500_000_000);

    private JobExecution buildExecution() {
        JobInstance instance = new JobInstance(42L, "dailyReconciliation");
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "feed.csv", true)
                .addLong("runId", 7L, false)
                .toJobParameters();
        JobExecution execution = new JobExecution(instance, 99L, params);
        execution.setCreateTime(CREATE);
        execution.setStartTime(START);
        execution.setEndTime(END);
        execution.setStatus(BatchStatus.FAILED);
        execution.setExitStatus(ExitStatus.FAILED.addExitDescription("job blew up"));
        execution.addFailureException(new IllegalStateException("boom at job level"));

        StepExecution step = execution.createStepExecution("processStep");
        step.setStatus(BatchStatus.FAILED);
        step.setReadCount(100L);
        step.setWriteCount(80L);
        step.setCommitCount(8L);
        step.setRollbackCount(1L);
        step.setReadSkipCount(2L);
        step.setWriteSkipCount(3L);
        step.setProcessSkipCount(4L);
        step.setFilterCount(5L);
        step.setStartTime(START);
        step.setEndTime(END);
        step.setExitStatus(ExitStatus.FAILED.addExitDescription("step failed"));
        step.addFailureException(new RuntimeException("kaboom at step level"));
        return execution;
    }

    @Test
    void listJobs_mapsNameInstanceCountAndLastExecution() throws Exception {
        JobExecution execution = buildExecution();
        JobInstance instance = execution.getJobInstance();
        when(jobExplorer.getJobNames()).thenReturn(List.of("dailyReconciliation"));
        when(jobExplorer.getJobInstanceCount("dailyReconciliation")).thenReturn(2L);
        when(jobExplorer.getLastJobInstance("dailyReconciliation")).thenReturn(instance);
        when(jobExplorer.getJobExecutions(instance)).thenReturn(List.of(execution));

        List<JobSummaryDto> jobs = service.listJobs();

        assertThat(jobs).hasSize(1);
        JobSummaryDto job = jobs.get(0);
        assertThat(job.name()).isEqualTo("dailyReconciliation");
        assertThat(job.instanceCount()).isEqualTo(2L);
        assertThat(job.lastExecution()).isNotNull();
        assertThat(job.lastExecution().status()).isEqualTo("FAILED");
        assertThat(job.lastExecution().exitCode()).isEqualTo("FAILED");
        assertThat(job.lastExecution().durationMs()).isEqualTo(1500L);
        assertThat(job.lastExecution().parameters())
                .extracting("name")
                .containsExactlyInAnyOrder("inputFile", "runId");
    }

    @Test
    void listJobs_neverRun_yieldsNullLastExecution() throws Exception {
        when(jobExplorer.getJobNames()).thenReturn(List.of("idleJob"));
        when(jobExplorer.getJobInstanceCount("idleJob")).thenReturn(0L);
        when(jobExplorer.getLastJobInstance("idleJob")).thenReturn(null);

        List<JobSummaryDto> jobs = service.listJobs();

        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).lastExecution()).isNull();
        assertThat(jobs.get(0).instanceCount()).isZero();
    }

    @Test
    void listJobs_includesRegisteredJobsThatNeverRan() throws Exception {
        // JobExplorer only knows executed jobs; registry contributes never-run names.
        org.springframework.batch.core.configuration.ListableJobLocator registry =
                mock(org.springframework.batch.core.configuration.ListableJobLocator.class);
        when(registry.getJobNames()).thenReturn(List.of("registeredOnlyJob", "ranJob"));
        when(jobExplorer.getJobNames()).thenReturn(List.of("ranJob"));
        when(jobExplorer.getJobInstanceCount("ranJob")).thenReturn(1L);
        when(jobExplorer.getJobInstanceCount("registeredOnlyJob"))
                .thenThrow(new NoSuchJobException("never ran"));
        when(jobExplorer.getLastJobInstance("ranJob")).thenReturn(null);
        when(jobExplorer.getLastJobInstance("registeredOnlyJob")).thenReturn(null);

        List<JobSummaryDto> jobs = new JobQueryService(jobExplorer, registry).listJobs();

        assertThat(jobs).extracting(JobSummaryDto::name)
                .containsExactly("ranJob", "registeredOnlyJob");
        JobSummaryDto neverRan = jobs.get(1);
        assertThat(neverRan.instanceCount()).isZero();
        assertThat(neverRan.lastExecution()).isNull();
    }

    @Test
    void listExecutions_sortsMostRecentFirst() {
        JobInstance instance = new JobInstance(1L, "dailyReconciliation");
        JobExecution older = new JobExecution(instance, 10L, new JobParameters());
        JobExecution newer = new JobExecution(instance, 20L, new JobParameters());
        when(jobExplorer.getJobInstances("dailyReconciliation", 0, 20)).thenReturn(List.of(instance));
        when(jobExplorer.getJobExecutions(instance)).thenReturn(List.of(older, newer));

        List<ExecutionSummaryDto> executions = service.listExecutions("dailyReconciliation", 0, 20);

        assertThat(executions).extracting(ExecutionSummaryDto::executionId).containsExactly(20L, 10L);
    }

    @Test
    void getExecutionDetail_mapsStepCountsAndStackTraces() {
        JobExecution execution = buildExecution();
        when(jobExplorer.getJobExecution(99L)).thenReturn(execution);

        ExecutionDetailDto detail = service.getExecutionDetail(99L);

        assertThat(detail.executionId()).isEqualTo(99L);
        assertThat(detail.jobName()).isEqualTo("dailyReconciliation");
        assertThat(detail.status()).isEqualTo("FAILED");
        assertThat(detail.exitDescription()).isEqualTo("job blew up");
        assertThat(detail.failureExceptions()).hasSize(1);
        assertThat(detail.failureExceptions().get(0)).contains("IllegalStateException", "boom at job level");

        assertThat(detail.steps()).hasSize(1);
        StepExecutionDto step = detail.steps().get(0);
        assertThat(step.stepName()).isEqualTo("processStep");
        assertThat(step.readCount()).isEqualTo(100L);
        assertThat(step.writeCount()).isEqualTo(80L);
        assertThat(step.commitCount()).isEqualTo(8L);
        assertThat(step.rollbackCount()).isEqualTo(1L);
        assertThat(step.readSkipCount()).isEqualTo(2L);
        assertThat(step.writeSkipCount()).isEqualTo(3L);
        assertThat(step.processSkipCount()).isEqualTo(4L);
        assertThat(step.filterCount()).isEqualTo(5L);
        assertThat(step.exitCode()).isEqualTo("FAILED");
        assertThat(step.durationMs()).isEqualTo(1500L);
        assertThat(step.failureExceptions()).hasSize(1);
        assertThat(step.failureExceptions().get(0)).contains("kaboom at step level");
    }

    @Test
    void getExecutionDetail_unknownId_throwsNotFound() {
        when(jobExplorer.getJobExecution(404L)).thenReturn(null);

        assertThatThrownBy(() -> service.getExecutionDetail(404L))
                .isInstanceOf(ExecutionNotFoundException.class);
    }
}
