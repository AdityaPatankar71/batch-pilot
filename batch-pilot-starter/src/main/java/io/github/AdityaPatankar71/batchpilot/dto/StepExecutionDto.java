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
package io.github.AdityaPatankar71.batchpilot.dto;

import java.util.List;

/**
 * Step-level execution detail, including read/write/skip/commit counts and any
 * failure stack traces captured by Spring Batch.
 */
public record StepExecutionDto(
        Long id,
        String stepName,
        String status,
        long readCount,
        long writeCount,
        long commitCount,
        long rollbackCount,
        long readSkipCount,
        long writeSkipCount,
        long processSkipCount,
        long filterCount,
        String exitCode,
        String exitDescription,
        String startTime,
        String endTime,
        Long durationMs,
        List<String> failureExceptions) {
}
