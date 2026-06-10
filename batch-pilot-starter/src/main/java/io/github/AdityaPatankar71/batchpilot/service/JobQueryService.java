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
import io.github.AdityaPatankar71.batchpilot.dto.JobParameterDto;
import io.github.AdityaPatankar71.batchpilot.dto.JobSummaryDto;
import io.github.AdityaPatankar71.batchpilot.dto.StepExecutionDto;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Read-only query layer over Spring Batch metadata.
 *
 * <p>All reads go through {@link JobExplorer}; this class never issues raw SQL
 * against the {@code BATCH_*} tables. It maps Spring Batch domain objects into
 * transport DTOs and is the single source of truth for the REST layer.
 */
public class JobQueryService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JobExplorer jobExplorer;
    private final ListableJobLocator jobRegistry;

    public JobQueryService(JobExplorer jobExplorer) {
        this(jobExplorer, null);
    }

    /**
     * @param jobExplorer source of execution history ({@code BATCH_*} metadata)
     * @param jobRegistry optional registry of registered jobs, so jobs that have
     *                    never run still appear in the overview (JobExplorer only
     *                    knows names that have at least one instance)
     */
    public JobQueryService(JobExplorer jobExplorer, ListableJobLocator jobRegistry) {
        this.jobExplorer = jobExplorer;
        this.jobRegistry = jobRegistry;
    }

    /** Every known job name (registered or executed) with instance count and last-execution summary. */
    public List<JobSummaryDto> listJobs() {
        TreeSet<String> names = new TreeSet<>(jobExplorer.getJobNames());
        if (jobRegistry != null) {
            names.addAll(jobRegistry.getJobNames());
        }
        List<JobSummaryDto> jobs = new ArrayList<>();
        for (String name : names) {
            long instanceCount = instanceCount(name);
            ExecutionSummaryDto last = lastExecutionSummary(name);
            jobs.add(new JobSummaryDto(name, instanceCount, last));
        }
        return jobs;
    }

    /**
     * Executions for a single job, most recent first.
     *
     * @param jobName job to list executions for
     * @param page    zero-based page index over job <em>instances</em>
     * @param size    number of instances per page
     */
    public List<ExecutionSummaryDto> listExecutions(String jobName, int page, int size) {
        int start = Math.max(0, page) * Math.max(1, size);
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, start, Math.max(1, size));
        List<ExecutionSummaryDto> executions = new ArrayList<>();
        for (JobInstance instance : instances) {
            for (JobExecution execution : jobExplorer.getJobExecutions(instance)) {
                executions.add(toSummary(execution));
            }
        }
        executions.sort(Comparator.comparing(ExecutionSummaryDto::executionId).reversed());
        return executions;
    }

    /** Full detail for one execution, or throw {@link ExecutionNotFoundException}. */
    public ExecutionDetailDto getExecutionDetail(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new ExecutionNotFoundException(executionId);
        }
        return toDetail(execution);
    }

    // --- mapping ---------------------------------------------------------

    private ExecutionSummaryDto lastExecutionSummary(String jobName) {
        JobInstance lastInstance = jobExplorer.getLastJobInstance(jobName);
        if (lastInstance == null) {
            return null;
        }
        JobExecution last = latestExecution(lastInstance);
        return last == null ? null : toSummary(last);
    }

    private JobExecution latestExecution(JobInstance instance) {
        return jobExplorer.getJobExecutions(instance).stream()
                .max(Comparator.comparing(JobExecution::getId))
                .orElse(null);
    }

    private long instanceCount(String jobName) {
        try {
            return jobExplorer.getJobInstanceCount(jobName);
        } catch (NoSuchJobException ex) {
            return 0L;
        }
    }

    private ExecutionSummaryDto toSummary(JobExecution e) {
        return new ExecutionSummaryDto(
                e.getId(),
                e.getJobInstance() == null ? null : e.getJobInstance().getInstanceId(),
                e.getJobInstance() == null ? null : e.getJobInstance().getJobName(),
                e.getStatus() == null ? null : e.getStatus().name(),
                e.getExitStatus() == null ? null : e.getExitStatus().getExitCode(),
                fmt(e.getCreateTime()),
                fmt(e.getStartTime()),
                fmt(e.getEndTime()),
                durationMs(e.getStartTime(), e.getEndTime()),
                mapParameters(e));
    }

    private ExecutionDetailDto toDetail(JobExecution e) {
        List<StepExecutionDto> steps = new ArrayList<>();
        for (StepExecution s : e.getStepExecutions()) {
            steps.add(toStep(s));
        }
        return new ExecutionDetailDto(
                e.getId(),
                e.getJobInstance() == null ? null : e.getJobInstance().getInstanceId(),
                e.getJobInstance() == null ? null : e.getJobInstance().getJobName(),
                e.getStatus() == null ? null : e.getStatus().name(),
                e.getExitStatus() == null ? null : e.getExitStatus().getExitCode(),
                e.getExitStatus() == null ? null : emptyToNull(e.getExitStatus().getExitDescription()),
                fmt(e.getCreateTime()),
                fmt(e.getStartTime()),
                fmt(e.getEndTime()),
                durationMs(e.getStartTime(), e.getEndTime()),
                mapParameters(e),
                steps,
                stackTraces(e.getFailureExceptions()));
    }

    private StepExecutionDto toStep(StepExecution s) {
        return new StepExecutionDto(
                s.getId(),
                s.getStepName(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getReadCount(),
                s.getWriteCount(),
                s.getCommitCount(),
                s.getRollbackCount(),
                s.getReadSkipCount(),
                s.getWriteSkipCount(),
                s.getProcessSkipCount(),
                s.getFilterCount(),
                s.getExitStatus() == null ? null : s.getExitStatus().getExitCode(),
                s.getExitStatus() == null ? null : emptyToNull(s.getExitStatus().getExitDescription()),
                fmt(s.getStartTime()),
                fmt(s.getEndTime()),
                durationMs(s.getStartTime(), s.getEndTime()),
                stackTraces(s.getFailureExceptions()));
    }

    private List<JobParameterDto> mapParameters(JobExecution e) {
        List<JobParameterDto> params = new ArrayList<>();
        if (e.getJobParameters() == null) {
            return params;
        }
        for (Map.Entry<String, JobParameter<?>> entry : e.getJobParameters().getParameters().entrySet()) {
            JobParameter<?> p = entry.getValue();
            params.add(new JobParameterDto(
                    entry.getKey(),
                    p.getValue() == null ? null : String.valueOf(p.getValue()),
                    p.getType() == null ? null : p.getType().getName(),
                    p.isIdentifying()));
        }
        return params;
    }

    private List<String> stackTraces(List<Throwable> throwables) {
        List<String> traces = new ArrayList<>();
        if (throwables == null) {
            return traces;
        }
        for (Throwable t : throwables) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            traces.add(sw.toString());
        }
        return traces;
    }

    private static Long durationMs(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return Duration.between(start, end).toMillis();
    }

    private static String fmt(LocalDateTime time) {
        return time == null ? null : time.format(ISO);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
