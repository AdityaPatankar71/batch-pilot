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
package io.github.AdityaPatankar71.batchpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration for batch-pilot. The console is opt-in: nothing is wired unless
 * {@code batch-pilot.enabled=true}.
 */
@ConfigurationProperties(prefix = "batch-pilot")
public class BatchPilotProperties {

    /** Master switch. When false (the default) batch-pilot contributes no beans. */
    private boolean enabled = false;

    @NestedConfigurationProperty
    private Security security = new Security();

    @NestedConfigurationProperty
    private Actions actions = new Actions();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    /** Security settings for the console endpoints. */
    public static class Security {

        /** When true (default) and Spring Security is present, /batch-pilot/** requires {@link #role}. */
        private boolean enabled = true;

        /** Role required to access the console. Maps to authority ROLE_&lt;role&gt;. */
        private String role = "BATCH_PILOT_ADMIN";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    /** Individually toggleable write actions. Default on; set false for view-only deployments. */
    public static class Actions {

        private boolean restart = true;
        private boolean stop = true;
        private boolean launch = true;

        public boolean isRestart() {
            return restart;
        }

        public void setRestart(boolean restart) {
            this.restart = restart;
        }

        public boolean isStop() {
            return stop;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        public boolean isLaunch() {
            return launch;
        }

        public void setLaunch(boolean launch) {
            this.launch = launch;
        }
    }
}
