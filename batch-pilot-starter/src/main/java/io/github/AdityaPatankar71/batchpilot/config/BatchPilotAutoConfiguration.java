package io.github.AdityaPatankar71.batchpilot.config;

import io.github.AdityaPatankar71.batchpilot.service.JobQueryService;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotController;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotWebConfig;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Opt-in auto-configuration for batch-pilot.
 *
 * <p>Active only when {@code batch-pilot.enabled=true}, Spring Batch is on the
 * classpath, and the application is a servlet web app. Wires the read-only
 * query service, REST controller, and the static-resource serving for the
 * Angular console.
 */
@AutoConfiguration
@ConditionalOnClass(JobExplorer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "batch-pilot", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(BatchPilotProperties.class)
@Import(BatchPilotWebConfig.class)
public class BatchPilotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JobQueryService batchPilotJobQueryService(JobExplorer jobExplorer) {
        return new JobQueryService(jobExplorer);
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchPilotController batchPilotController(JobQueryService jobQueryService) {
        return new BatchPilotController(jobQueryService);
    }
}
