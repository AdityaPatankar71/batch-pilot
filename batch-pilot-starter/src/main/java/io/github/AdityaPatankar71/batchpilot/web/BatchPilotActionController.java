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

import io.github.AdityaPatankar71.batchpilot.action.ActionResults.ActionsCapabilities;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.LaunchResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.RestartResult;
import io.github.AdityaPatankar71.batchpilot.action.ActionResults.StopResult;
import io.github.AdityaPatankar71.batchpilot.action.JobOperationService;
import io.github.AdityaPatankar71.batchpilot.action.LaunchRequest;
import io.github.AdityaPatankar71.batchpilot.audit.AuditEventDto;
import io.github.AdityaPatankar71.batchpilot.audit.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Write actions (restart / stop / launch), action capabilities, and the audit
 * log. All mutations are delegated to {@link JobOperationService}, which routes
 * through Spring Batch's {@code JobOperator} and records every attempt.
 */
@RestController
@RequestMapping("/batch-pilot/api")
public class BatchPilotActionController {

    private final JobOperationService operations;
    private final AuditService audit;

    public BatchPilotActionController(JobOperationService operations, AuditService audit) {
        this.operations = operations;
        this.audit = audit;
    }

    /** Which write actions are currently enabled. */
    @GetMapping("/actions")
    public ActionsCapabilities actions() {
        return operations.capabilities();
    }

    @PostMapping("/executions/{executionId}/restart")
    public RestartResult restart(@PathVariable long executionId, Principal principal) {
        return operations.restart(executionId, name(principal));
    }

    @PostMapping("/executions/{executionId}/stop")
    public StopResult stop(@PathVariable long executionId, Principal principal) {
        return operations.stop(executionId, name(principal));
    }

    @PostMapping("/jobs/{jobName}/launch")
    public LaunchResult launch(@PathVariable String jobName,
                               @RequestBody(required = false) LaunchRequest request,
                               Principal principal) {
        LaunchRequest body = request == null ? new LaunchRequest(List.of()) : request;
        return operations.launch(jobName, body, name(principal));
    }

    @GetMapping("/audit")
    public List<AuditEventDto> audit(@RequestParam(defaultValue = "100") int limit) {
        return audit.list(limit);
    }

    private static String name(Principal principal) {
        return principal == null ? "anonymous" : principal.getName();
    }
}
