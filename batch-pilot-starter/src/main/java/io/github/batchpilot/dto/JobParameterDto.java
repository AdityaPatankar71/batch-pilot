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
package io.github.batchpilot.dto;

/**
 * A single Spring Batch job parameter, flattened for transport.
 *
 * @param name        parameter key
 * @param value       string form of the parameter value
 * @param type        fully-qualified type of the parameter value
 * @param identifying whether the parameter contributes to the job instance identity
 */
public record JobParameterDto(String name, String value, String type, boolean identifying) {
}
