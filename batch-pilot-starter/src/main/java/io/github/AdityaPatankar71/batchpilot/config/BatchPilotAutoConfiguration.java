package io.github.AdityaPatankar71.batchpilot.config;

import io.github.AdityaPatankar71.batchpilot.action.JobOperationService;
import io.github.AdityaPatankar71.batchpilot.audit.AuditService;
import io.github.AdityaPatankar71.batchpilot.service.JobQueryService;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotActionController;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotController;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotExceptionHandler;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotWebConfig;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Opt-in auto-configuration for batch-pilot.
 *
 * <p>Active only when {@code batch-pilot.enabled=true}, Spring Batch is on the
 * classpath, and the application is a servlet web app. Wires the read-only query
 * service, the write-action stack (JobOperator-backed), the audit log, the REST
 * controllers, and static-resource serving for the Angular console.
 */
@AutoConfiguration
@ConditionalOnClass(JobExplorer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "batch-pilot", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(BatchPilotProperties.class)
@Import(BatchPilotWebConfig.class)
public class BatchPilotAutoConfiguration {

    // --- read side -------------------------------------------------------

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

    // --- audit & write side ---------------------------------------------

    @Bean
    @ConditionalOnMissingBean
    public AuditService batchPilotAuditService(DataSource dataSource) {
        return new AuditService(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean
    public JobOperationService batchPilotJobOperationService(JobOperator jobOperator,
                                                             BatchPilotProperties properties,
                                                             AuditService auditService) {
        // Uses the host application's JobOperator (Spring Boot auto-configures one).
        return new JobOperationService(jobOperator, properties, auditService);
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchPilotActionController batchPilotActionController(JobOperationService operations,
                                                                AuditService auditService) {
        return new BatchPilotActionController(operations, auditService);
    }

    @Bean
    public BatchPilotExceptionHandler batchPilotExceptionHandler() {
        return new BatchPilotExceptionHandler();
    }
}
