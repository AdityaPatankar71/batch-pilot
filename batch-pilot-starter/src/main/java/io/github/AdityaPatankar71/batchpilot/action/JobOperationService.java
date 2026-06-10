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
package io.github.AdityaPatankar71.batchpilot.action;

import io.github.AdityaPatankar71.batchpilot.action.ActionResults.ActionsCapabilities;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.LaunchResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.RestartResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.StopResult;
import io.github.AdityaPatankar71.batchpilot.action.LaunchRequest.LaunchParam;
import io.github.AdityaPatankar71.batchpilot.audit.AuditAction;
import io.github.AdityaPatankar71.batchpilot.audit.AuditService;
import io.github.AdityaPatankar71.batchpilot.config.BatchPilotProperties;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.converter.JobParametersConversionException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;

import java.util.Properties;

/**
 * Executes write actions (restart / stop / launch) through Spring Batch's own
 * {@link JobOperator}, gated by the per-action toggles, with every attempt and
 * its outcome written to the audit log. Framework exceptions are translated into
 * {@link ActionException}s carrying an appropriate HTTP status.
 */
public class JobOperationService {

    private final JobOperator jobOperator;
    private final BatchPilotProperties properties;
    private final AuditService audit;

    public JobOperationService(JobOperator jobOperator,
                               BatchPilotProperties properties,
                               AuditService audit) {
        this.jobOperator = jobOperator;
        this.properties = properties;
        this.audit = audit;
    }

    public ActionsCapabilities capabilities() {
        BatchPilotProperties.Actions a = properties.getActions();
        return new ActionsCapabilities(a.isRestart(), a.isStop(), a.isLaunch());
    }

    public RestartResult restart(long executionId, String principal) {
        requireEnabled(properties.getActions().isRestart(), "restart");
        String target = "execution:" + executionId;
        try {
            Long newId = jobOperator.restart(executionId);
            audit.record(principal, AuditAction.RESTART, target, true, "newExecutionId=" + newId);
            return new RestartResult(newId);
        } catch (Exception ex) {
            audit.record(principal, AuditAction.RESTART, target, false, ex.getMessage());
            throw translate(ex);
        }
    }

    public StopResult stop(long executionId, String principal) {
        requireEnabled(properties.getActions().isStop(), "stop");
        String target = "execution:" + executionId;
        try {
            boolean stopped = jobOperator.stop(executionId);
            audit.record(principal, AuditAction.STOP, target, true, "stopRequested=" + stopped);
            return new StopResult(stopped);
        } catch (Exception ex) {
            audit.record(principal, AuditAction.STOP, target, false, ex.getMessage());
            throw translate(ex);
        }
    }

    public LaunchResult launch(String jobName, LaunchRequest request, String principal) {
        requireEnabled(properties.getActions().isLaunch(), "launch");
        String target = "job:" + jobName;
        try {
            Properties params = toProperties(request);
            Long executionId = jobOperator.start(jobName, params);
            audit.record(principal, AuditAction.LAUNCH, target, true, "executionId=" + executionId);
            return new LaunchResult(executionId);
        } catch (ActionException ex) {
            audit.record(principal, AuditAction.LAUNCH, target, false, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            audit.record(principal, AuditAction.LAUNCH, target, false, ex.getMessage());
            throw translate(ex);
        }
    }

    private void requireEnabled(boolean enabled, String action) {
        if (!enabled) {
            throw new ActionDisabledException(action);
        }
    }

    /**
     * Encodes typed parameters in the {@code value,type,identifying} form used by
     * Spring's {@code DefaultJobParametersConverter} (the converter Spring Boot's
     * {@code JobOperator} is wired with), e.g. {@code runId=7,java.lang.Long,true}.
     */
    private Properties toProperties(LaunchRequest request) {
        Properties props = new Properties();
        for (LaunchParam p : request.parameters()) {
            if (p == null || p.name() == null || p.name().isBlank()) {
                continue;
            }
            String value = p.value() == null ? "" : p.value();
            if (value.contains(",")) {
                throw new ActionException(HttpStatus.BAD_REQUEST,
                        "Parameter '" + p.name() + "' value may not contain a comma");
            }
            props.setProperty(p.name(), value + "," + p.typeOrString().javaType() + "," + p.isIdentifying());
        }
        return props;
    }

    private ActionException translate(Throwable ex) {
        if (ex instanceof ActionException ae) {
            return ae;
        }
        if (ex instanceof JobInstanceAlreadyCompleteException) {
            return new ActionException(HttpStatus.CONFLICT,
                    "Execution's instance has already completed and cannot be restarted");
        }
        if (ex instanceof JobExecutionAlreadyRunningException) {
            return new ActionException(HttpStatus.CONFLICT, "An execution for this instance is already running");
        }
        if (ex instanceof JobExecutionNotRunningException) {
            return new ActionException(HttpStatus.CONFLICT, "Execution is not running");
        }
        if (ex instanceof JobInstanceAlreadyExistsException) {
            return new ActionException(HttpStatus.CONFLICT,
                    "A job instance with these identifying parameters already exists");
        }
        if (ex instanceof NoSuchJobExecutionException) {
            return new ActionException(HttpStatus.NOT_FOUND, "No such job execution");
        }
        if (ex instanceof NoSuchJobException) {
            return new ActionException(HttpStatus.NOT_FOUND, "No such job");
        }
        if (ex instanceof JobParametersInvalidException) {
            return new ActionException(HttpStatus.BAD_REQUEST, "Invalid job parameters: " + ex.getMessage());
        }
        if (ex instanceof JobParametersConversionException) {
            return new ActionException(HttpStatus.BAD_REQUEST, "Invalid job parameters: " + ex.getMessage());
        }
        if (ex instanceof JobRestartException) {
            return new ActionException(HttpStatus.CONFLICT, "Execution cannot be restarted: " + ex.getMessage());
        }
        return new ActionException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Action failed: " + ex.getClass().getSimpleName() + " " + ex.getMessage());
    }
}
