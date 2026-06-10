package io.github.AdityaPatankar71.batchpilot.web;

import io.github.AdityaPatankar71.batchpilot.action.ActionException;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.ActionsCapabilities;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.LaunchResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.RestartResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.StopResult;
import io.github.AdityaPatankar71.batchpilot.action.JobOperationService;
import io.github.AdityaPatankar71.batchpilot.audit.AuditEventDto;
import io.github.AdityaPatankar71.batchpilot.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BatchPilotActionControllerTest {

    private final JobOperationService operations = mock(JobOperationService.class);
    private final AuditService audit = mock(AuditService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BatchPilotActionController(operations, audit))
                .setControllerAdvice(new BatchPilotExceptionHandler())
                .build();
    }

    @Test
    void actions_returnsCapabilities() throws Exception {
        when(operations.capabilities()).thenReturn(new ActionsCapabilities(true, false, true));

        mockMvc.perform(get("/batch-pilot/api/actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restart").value(true))
                .andExpect(jsonPath("$.stop").value(false))
                .andExpect(jsonPath("$.launch").value(true));
    }

    @Test
    void restart_returnsNewExecutionId() throws Exception {
        when(operations.restart(eq(99L), anyString())).thenReturn(new RestartResult(123L));

        mockMvc.perform(post("/batch-pilot/api/executions/99/restart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newExecutionId").value(123));
    }

    @Test
    void restart_conflict_isMappedByAdvice() throws Exception {
        when(operations.restart(eq(99L), anyString()))
                .thenThrow(new ActionException(HttpStatus.CONFLICT, "already completed"));

        mockMvc.perform(post("/batch-pilot/api/executions/99/restart"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("already completed"));
    }

    @Test
    void stop_returnsFlag() throws Exception {
        when(operations.stop(eq(5L), anyString())).thenReturn(new StopResult(true));

        mockMvc.perform(post("/batch-pilot/api/executions/5/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stopRequested").value(true));
    }

    @Test
    void launch_returnsExecutionId() throws Exception {
        when(operations.launch(eq("dailyJob"), any(), anyString())).thenReturn(new LaunchResult(77L));

        mockMvc.perform(post("/batch-pilot/api/jobs/dailyJob/launch")
                        .contentType("application/json")
                        .content("{\"parameters\":[{\"name\":\"runId\",\"value\":\"7\",\"type\":\"LONG\",\"identifying\":true}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(77));
    }

    @Test
    void audit_returnsEvents() throws Exception {
        when(audit.list(100)).thenReturn(List.of(
                new AuditEventDto("id-1", "2026-06-10T09:00:00", "alice", "RESTART",
                        "execution:99", "SUCCESS", "newExecutionId=123")));

        mockMvc.perform(get("/batch-pilot/api/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].principal").value("alice"))
                .andExpect(jsonPath("$[0].action").value("RESTART"))
                .andExpect(jsonPath("$[0].result").value("SUCCESS"));
    }
}
