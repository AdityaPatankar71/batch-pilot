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
package io.github.batchpilot.action;

/** Small response payloads for the action endpoints. */
public final class ActionResults {

    private ActionResults() {
    }

    public record RestartResult(Long newExecutionId) {
    }

    public record StopResult(boolean stopRequested) {
    }

    public record LaunchResult(Long executionId) {
    }

    /** Which write actions are currently enabled, so the UI can show/hide controls. */
    public record ActionsCapabilities(boolean restart, boolean stop, boolean launch) {
    }
}
