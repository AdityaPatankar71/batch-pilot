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

import java.util.List;

/** Body of a launch request: the typed parameters to start a job with. */
public record LaunchRequest(List<LaunchParam> parameters) {

    public List<LaunchParam> parameters() {
        return parameters == null ? List.of() : parameters;
    }

    /**
     * A single typed launch parameter.
     *
     * @param name        parameter key
     * @param value       string form of the value, converted according to {@link #type}
     * @param type        parameter type
     * @param identifying whether it contributes to the job instance identity (defaults true)
     */
    public record LaunchParam(String name, String value, ParamType type, Boolean identifying) {

        public boolean isIdentifying() {
            return identifying == null || identifying;
        }

        public ParamType typeOrString() {
            return type == null ? ParamType.STRING : type;
        }
    }

    /** Supported launch parameter types, mapped to the Java types Spring Batch will convert to. */
    public enum ParamType {
        STRING("java.lang.String"),
        LONG("java.lang.Long"),
        DOUBLE("java.lang.Double"),
        DATE("java.time.LocalDate");

        private final String javaType;

        ParamType(String javaType) {
            this.javaType = javaType;
        }

        public String javaType() {
            return javaType;
        }
    }
}
