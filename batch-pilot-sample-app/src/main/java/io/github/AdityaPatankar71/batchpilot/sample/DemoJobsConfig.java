package io.github.AdityaPatankar71.batchpilot.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
 * Demo jobs so the console has real data on first start:
 * <ul>
 *   <li>{@code successJob} – a single chunk step that completes.</li>
 *   <li>{@code multiStepJob} – extract (chunk), transform (tasklet), load (chunk).</li>
 *   <li>{@code failingJob} – a chunk step that throws, producing a stack trace.</li>
 *   <li>{@code slowJob} – ~30s run (1 item/second), for watching STARTED status
 *       and exercising stop; never launched automatically.</li>
 * </ul>
 *
 * <p>All readers are {@link StepScope step-scoped} so every execution gets a fresh
 * reader. {@link ListItemReader} is stateful; as a singleton it would be exhausted
 * after the first run and every relaunch/restart would silently read zero items.
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
    @StepScope
    public ItemReader<Integer> customersReader() {
        return new ListItemReader<>(range(10));
    }

    @Bean
    public Step loadCustomersStep(JobRepository jobRepository, PlatformTransactionManager tx,
                                  ItemReader<Integer> customersReader) {
        return new StepBuilder("loadCustomers", jobRepository)
                .<Integer, Integer>chunk(5, tx)
                .reader(customersReader)
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
    @StepScope
    public ItemReader<Integer> extractReader() {
        return new ListItemReader<>(range(5));
    }

    @Bean
    public Step extractStep(JobRepository jobRepository, PlatformTransactionManager tx,
                            ItemReader<Integer> extractReader) {
        return new StepBuilder("extract", jobRepository)
                .<Integer, Integer>chunk(2, tx)
                .reader(extractReader)
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
    @StepScope
    public ItemReader<Integer> loadReader() {
        return new ListItemReader<>(range(3));
    }

    @Bean
    public Step loadStep(JobRepository jobRepository, PlatformTransactionManager tx,
                         ItemReader<Integer> loadReader) {
        return new StepBuilder("load", jobRepository)
                .<Integer, Integer>chunk(3, tx)
                .reader(loadReader)
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
    @StepScope
    public ItemReader<Integer> riskyReader() {
        return new ListItemReader<>(range(5));
    }

    @Bean
    public Step riskyStep(JobRepository jobRepository, PlatformTransactionManager tx,
                          ItemReader<Integer> riskyReader) {
        ItemWriter<Integer> explodingWriter = chunk -> {
            if (chunk.getItems().contains(3)) {
                throw new IllegalStateException("Downstream system rejected record id=3");
            }
            log.info("[risky] wrote {} items", chunk.size());
        };
        return new StepBuilder("risky", jobRepository)
                .<Integer, Integer>chunk(2, tx)
                .reader(riskyReader)
                .writer(explodingWriter)
                .build();
    }

    // --- slowJob ----------------------------------------------------------

    @Bean
    public Job slowJob(JobRepository jobRepository, Step crawlStep) {
        return new JobBuilder("slowJob", jobRepository)
                .start(crawlStep)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Integer> crawlReader() {
        return new ListItemReader<>(range(30));
    }

    @Bean
    public Step crawlStep(JobRepository jobRepository, PlatformTransactionManager tx,
                          ItemReader<Integer> crawlReader) {
        // One item per chunk, one second per item: stop requests are honoured at
        // chunk boundaries, so this step stops within ~a second of the request.
        ItemWriter<Integer> slowWriter = chunk -> {
            Thread.sleep(1000);
            log.info("[crawl] processed item {}", chunk.getItems());
        };
        return new StepBuilder("crawl", jobRepository)
                .<Integer, Integer>chunk(1, tx)
                .reader(crawlReader)
                .writer(slowWriter)
                .build();
    }
}
