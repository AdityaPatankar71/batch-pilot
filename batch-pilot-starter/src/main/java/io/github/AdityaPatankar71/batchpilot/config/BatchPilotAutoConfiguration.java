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

import io.github.AdityaPatankar71.batchpilot.action.JobOperationService;
import io.github.AdityaPatankar71.batchpilot.audit.AuditService;
import io.github.AdityaPatankar71.batchpilot.service.JobQueryService;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotActionController;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotController;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotExceptionHandler;
import io.github.AdityaPatankar71.batchpilot.web.BatchPilotWebConfig;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

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
    public JobQueryService batchPilotJobQueryService(JobExplorer jobExplorer,
                                                     ObjectProvider<JobRegistry> jobRegistry) {
        return new JobQueryService(jobExplorer, jobRegistry.getIfAvailable());
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

    /**
     * batch-pilot's own {@link JobOperator}, backed by an <em>asynchronous</em>
     * launcher so launch/restart REST calls return immediately with the new
     * execution id instead of blocking the HTTP thread until the job finishes
     * (which would also make stop-from-the-console impossible).
     *
     * <p>{@code defaultCandidate = false} keeps this bean out of by-type
     * autowiring, so a host application that injects {@code JobOperator} still
     * gets its own (Boot's) bean unambiguously.
     */
    @Bean(defaultCandidate = false)
    public JobOperatorFactoryBean batchPilotJobOperator(JobRepository jobRepository,
                                                        JobRegistry jobRegistry,
                                                        JobExplorer jobExplorer,
                                                        PlatformTransactionManager transactionManager) throws Exception {
        TaskExecutorJobLauncher asyncLauncher = new TaskExecutorJobLauncher();
        asyncLauncher.setJobRepository(jobRepository);
        asyncLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor("batch-pilot-"));
        asyncLauncher.afterPropertiesSet();

        JobOperatorFactoryBean factory = new JobOperatorFactoryBean();
        factory.setJobRepository(jobRepository);
        factory.setJobRegistry(jobRegistry);
        factory.setJobExplorer(jobExplorer);
        factory.setJobLauncher(asyncLauncher);
        factory.setTransactionManager(transactionManager);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public JobOperationService batchPilotJobOperationService(
            @Qualifier("batchPilotJobOperator") JobOperator jobOperator,
            BatchPilotProperties properties,
            AuditService auditService) {
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
