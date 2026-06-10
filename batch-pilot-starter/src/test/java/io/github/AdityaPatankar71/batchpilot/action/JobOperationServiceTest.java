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

import io.github.AdityaPatankar71.batchpilot.action.ActionResults.LaunchResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.RestartResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.StopResult;
import io.github.AdityaPatankar71.batchpilot.action.LaunchRequest.LaunchParam;
import io.github.AdityaPatankar71.batchpilot.action.LaunchRequest.ParamType;
import io.github.AdityaPatankar71.batchpilot.audit.AuditAction;
import io.github.AdityaPatankar71.batchpilot.audit.AuditService;
import io.github.AdityaPatankar71.batchpilot.config.BatchPilotProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobOperationServiceTest {

    private final JobOperator jobOperator = mock(JobOperator.class);
    private final AuditService audit = mock(AuditService.class);
    private final BatchPilotProperties properties = new BatchPilotProperties();
    private final JobOperationService service =
            new JobOperationService(jobOperator, properties, audit);

    @Test
    void restart_success_returnsNewIdAndAuditsSuccess() throws Exception {
        when(jobOperator.restart(99L)).thenReturn(123L);

        RestartResult result = service.restart(99L, "alice");

        assertThat(result.newExecutionId()).isEqualTo(123L);
        verify(audit).record(eq("alice"), eq(AuditAction.RESTART), eq("execution:99"), eq(true), anyString());
    }

    @Test
    void restart_disabled_throwsAndDoesNotCallOperator() throws Exception {
        properties.getActions().setRestart(false);

        assertThatThrownBy(() -> service.restart(99L, "alice"))
                .isInstanceOf(ActionDisabledException.class);
        verify(jobOperator, never()).restart(anyLong());
        verify(audit, never()).record(anyString(), any(), anyString(), anyBoolean(), anyString());
    }

    @Test
    void restart_completedInstance_mapsToConflictAndAuditsFailure() throws Exception {
        when(jobOperator.restart(99L)).thenThrow(new JobInstanceAlreadyCompleteException("done"));

        assertThatThrownBy(() -> service.restart(99L, "alice"))
                .isInstanceOfSatisfying(ActionException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(audit).record(eq("alice"), eq(AuditAction.RESTART), eq("execution:99"), eq(false), anyString());
    }

    @Test
    void stop_notRunning_mapsToConflict() throws Exception {
        when(jobOperator.stop(5L)).thenThrow(new JobExecutionNotRunningException("not running"));

        assertThatThrownBy(() -> service.stop(5L, "bob"))
                .isInstanceOfSatisfying(ActionException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(audit).record(eq("bob"), eq(AuditAction.STOP), eq("execution:5"), eq(false), anyString());
    }

    @Test
    void stop_success_returnsFlag() throws Exception {
        when(jobOperator.stop(5L)).thenReturn(true);

        StopResult result = service.stop(5L, "bob");

        assertThat(result.stopRequested()).isTrue();
        verify(audit).record(eq("bob"), eq(AuditAction.STOP), eq("execution:5"), eq(true), anyString());
    }

    @Test
    void launch_encodesTypedParametersAsJsonProperties() throws Exception {
        ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);
        when(jobOperator.start(eq("dailyJob"), captor.capture())).thenReturn(77L);
        LaunchRequest request = new LaunchRequest(List.of(
                new LaunchParam("runId", "7", ParamType.LONG, true),
                new LaunchParam("label", "nightly", ParamType.STRING, false)));

        LaunchResult result = service.launch("dailyJob", request, "carol");

        assertThat(result.executionId()).isEqualTo(77L);
        Properties sent = captor.getValue();
        assertThat(sent.getProperty("runId")).isEqualTo("7,java.lang.Long,true");
        assertThat(sent.getProperty("label")).isEqualTo("nightly,java.lang.String,false");
        verify(audit).record(eq("carol"), eq(AuditAction.LAUNCH), eq("job:dailyJob"), eq(true), anyString());
    }

    @Test
    void launch_unconvertibleParameterValue_mapsToBadRequest() throws Exception {
        when(jobOperator.start(eq("dailyJob"), any(Properties.class)))
                .thenThrow(new org.springframework.batch.core.converter.JobParametersConversionException(
                        "Unable to convert job parameter abc to type class java.lang.Long"));
        LaunchRequest request = new LaunchRequest(List.of(
                new LaunchParam("n", "abc", ParamType.LONG, true)));

        assertThatThrownBy(() -> service.launch("dailyJob", request, "carol"))
                .isInstanceOfSatisfying(ActionException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(audit).record(eq("carol"), eq(AuditAction.LAUNCH), eq("job:dailyJob"), eq(false), anyString());
    }

    @Test
    void launch_disabled_throws() {
        properties.getActions().setLaunch(false);

        assertThatThrownBy(() -> service.launch("dailyJob", new LaunchRequest(List.of()), "carol"))
                .isInstanceOf(ActionDisabledException.class);
    }
}
