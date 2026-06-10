# batch-pilot 0.1.0 — the missing Spring Batch console

First public release of **batch-pilot** — the operations console Spring Batch
has lacked since Spring Batch Admin was archived in 2019. See every job, drill
into every failure, restart/launch/stop — from one dependency, no new infra.

## Highlights

- **Job overview** — every registered job, last status, last run, duration, instance count.
- **Execution history** — per-job, with full job parameters.
- **Step drill-down** — read/write/skip/commit counts, exit status, full failure stack traces.
- **Actions** — restart failed/stopped executions, launch with a typed parameter builder, stop running ones (each individually toggleable).
- **Action audit log** — who did what, when.
- **Light & dark mode** — follows OS preference, toggle in the toolbar.
- **Secure by default** — does nothing unless `batch-pilot.enabled=true`; role-gated; write actions off-switchable for view-only deployments.

## Install

```xml
<dependency>
  <groupId>io.github.batchpilot</groupId>
  <artifactId>batch-pilot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```
```properties
batch-pilot.enabled=true
batch-pilot.security.role=BATCH_PILOT_ADMIN
```
Open `http://localhost:8080/batch-pilot`.

## Requirements

- Java 17+
- Spring Boot 3.x · Spring Batch 5 (a persistent `JobRepository` / `BATCH_*` tables)

## Try the demo

```bash
git clone https://github.com/AdityaPatankar71/batch-pilot.git
cd batch-pilot
mvn -pl batch-pilot-sample-app -am spring-boot:run
# http://localhost:8080/batch-pilot — success/multi-step/failing/slow jobs preloaded
```

## Reads everything from the metadata you already have

All views come from your app's `BATCH_*` tables via `JobExplorer`; all actions go
through Spring Batch's own `JobOperator`. No agents, no extra database.

## Links

- [README](https://github.com/AdityaPatankar71/batch-pilot#readme) · [Configuration reference](https://github.com/AdityaPatankar71/batch-pilot#configuration-reference) · [REST API](https://github.com/AdityaPatankar71/batch-pilot#rest-api)
- [Contributing](https://github.com/AdityaPatankar71/batch-pilot/blob/main/CONTRIBUTING.md) · [good first issues](https://github.com/AdityaPatankar71/batch-pilot/blob/main/.github/GOOD_FIRST_ISSUES.md)

First release — issues and feedback very welcome. Licensed under Apache-2.0.
