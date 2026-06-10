package io.github.AdityaPatankar71.batchpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for batch-pilot. The console is opt-in: nothing is wired unless
 * {@code batch-pilot.enabled=true}.
 */
@ConfigurationProperties(prefix = "batch-pilot")
public class BatchPilotProperties {

    /** Master switch. When false (the default) batch-pilot contributes no beans. */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
