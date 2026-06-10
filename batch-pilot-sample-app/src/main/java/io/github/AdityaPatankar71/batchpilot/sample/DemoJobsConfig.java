package io.github.AdityaPatankar71.batchpilot.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Three demo jobs so the console has real data on first start:
 * <ul>
 *   <li>{@code successJob} – a single chunk step that completes.</li>
 *   <li>{@code multiStepJob} – extract (chunk), transform (tasklet), load (chunk).</li>
 *   <li>{@code failingJob} – a chunk step that throws, producing a stack trace.</li>
 * </ul>
 */
@Configuration
public class DemoJobsConfig {

    private static final Logger log = LoggerFactory.getLogger(DemoJobsConfig.class);

    private static List<Integer> range(int count) {
        return IntStream.rangeClosed(1, count).boxed().toList();
    }

    private <T> ItemWriter<T> loggingWriter(String label) {
        return chunk -> log.info("[{}] wrote {} items: {}", label, chunk.size(), chunk.getItems());
    }

    // --- successJob ------------------------------------------------------

    @Bean
    public Job successJob(JobRepository jobRepository, Step loadCustomersStep) {
        return new JobBuilder("successJob", jobRepository)
                .start(loadCustomersStep)
                .build();
    }

    @Bean
    public Step loadCustomersStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        ItemReader<Integer> reader = new ListItemReader<>(range(10));
        return new StepBuilder("loadCustomers", jobRepository)
                .<Integer, Integer>chunk(5, tx)
                .reader(reader)
                .writer(loggingWriter("loadCustomers"))
                .build();
    }

    // --- multiStepJob ----------------------------------------------------

    @Bean
    public Job multiStepJob(JobRepository jobRepository, Step extractStep, Step transformStep, Step loadStep) {
        return new JobBuilder("multiStepJob", jobRepository)
                .start(extractStep)
                .next(transformStep)
                .next(loadStep)
                .build();
    }

    @Bean
    public Step extractStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("extract", jobRepository)
                .<Integer, Integer>chunk(2, tx)
                .reader(new ListItemReader<>(range(5)))
                .writer(loggingWriter("extract"))
                .build();
    }

    @Bean
    public Step transformStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        Tasklet tasklet = (contribution, context) -> {
            log.info("[transform] normalising records");
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("transform", jobRepository)
                .tasklet(tasklet, tx)
                .build();
    }

    @Bean
    public Step loadStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("load", jobRepository)
                .<Integer, Integer>chunk(3, tx)
                .reader(new ListItemReader<>(range(3)))
                .writer(loggingWriter("load"))
                .build();
    }

    // --- failingJob ------------------------------------------------------

    @Bean
    public Job failingJob(JobRepository jobRepository, Step riskyStep) {
        return new JobBuilder("failingJob", jobRepository)
                .start(riskyStep)
                .build();
    }

    @Bean
    public Step riskyStep(JobRepository jobRepository, PlatformTransactionManager tx) {
        ItemReader<Integer> reader = new ListItemReader<>(range(5));
        ItemWriter<Integer> explodingWriter = chunk -> {
            if (chunk.getItems().contains(3)) {
                throw new IllegalStateException("Downstream system rejected record id=3");
            }
            log.info("[risky] wrote {} items", chunk.size());
        };
        return new StepBuilder("risky", jobRepository)
                .<Integer, Integer>chunk(2, tx)
                .reader(reader)
                .writer(explodingWriter)
                .build();
    }
}
