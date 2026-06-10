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
package io.github.AdityaPatankar71.batchpilot.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Launches each demo job once at startup so the console shows a completed run, a
 * multi-step run, and a failed run the moment the app is up.
 *
 * <p>Boot's own job runner is disabled ({@code spring.batch.job.enabled=false})
 * so launches happen here, with unique parameters per job and the failure
 * contained so startup still succeeds.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final JobLauncher jobLauncher;
    private final Job successJob;
    private final Job multiStepJob;
    private final Job failingJob;

    public DemoDataLoader(JobLauncher jobLauncher, Job successJob, Job multiStepJob, Job failingJob) {
        this.jobLauncher = jobLauncher;
        this.successJob = successJob;
        this.multiStepJob = multiStepJob;
        this.failingJob = failingJob;
    }

    @Override
    public void run(String... args) {
        launch(successJob);
        launch(multiStepJob);
        launch(failingJob);
    }

    private void launch(Job job) {
        JobParameters params = new JobParametersBuilder()
                .addLong("launch.time", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(job, params);
        } catch (Exception ex) {
            // Expected for failingJob; keep startup healthy so the UI has the failure to show.
            log.warn("Demo job '{}' did not complete: {}", job.getName(), ex.getMessage());
        }
    }
}
