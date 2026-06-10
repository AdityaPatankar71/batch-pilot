# batch-pilot

> **The missing operations console for Spring Batch.** See every job, drill into every failure, restart with one click — without adopting a heavyweight platform.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.batchpilot/batch-pilot-starter.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.batchpilot/batch-pilot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

`Status: 0.1.0 — on Maven Central` · `Java 17+ · Spring Boot 3.x · Angular 18`

---

## Screenshots

> Demo GIF and screenshots are captured against `batch-pilot-sample-app` — see
> [`docs/screenshots/README.md`](docs/screenshots/README.md) for the shot list.

| Jobs overview | Step drill-down & failures | Dark mode |
|---|---|---|
| ![Jobs overview](docs/screenshots/jobs.png) | ![Execution detail](docs/screenshots/execution-detail.png) | ![Dark mode](docs/screenshots/dark-mode.png) |

![Restart a failed job](docs/screenshots/demo.gif)

---

## Why this exists

Spring Batch runs the world's nightly processing — banking settlements, insurance feeds, billing runs — but since **Spring Batch Admin was archived in 2019** (last stable release: 2015), there has been no free, lightweight way to *operate* those jobs:

- **Spring Cloud Data Flow**, the official replacement, is a full orchestration platform: its own server, its own deployment model. Massive overkill for "show me last night's failures and let me restart them."
- **Spring Boot Admin** shows health and metrics, but offers almost nothing batch-specific.
- **spring-batch-dashboard** (the best modern alternative) is *deliberately read-only* — a viewer, not a console.

The everyday operational loop — *which job failed, on which step, why, restart it* — has no owner. batch-pilot owns it.

## What it does (MVP)

- **Job overview** — every registered job, last execution status, last run time, duration trend sparkline.
- **Execution history** — filterable by job, status, date range; full job parameters per execution.
- **Step drill-down** — read/write/skip/commit counts per step, exit status, full failure stack traces.
- **Restart** failed/stopped executions (via `JobOperator.restart`).
- **Launch** a job with a guided parameter builder (typed params, identifying flag).
- **Stop** a running execution.
- **Action audit log** — who restarted/launched/stopped what, when.
- **Light & dark mode** — follows your OS preference, toggle in the toolbar.

### Post-MVP roadmap
- Failure alerts (webhook / email) and missed-run detection
- Duration anomaly highlighting ("this job is 3× slower than its 30-day median")
- Standalone read-only mode: point batch-pilot at any `BATCH_*` metadata schema of apps you can't modify
- Multi-application federation (one console, many batch apps)

## How it works

Two modules, zero new infrastructure:

```
batch-pilot/
├── batch-pilot-starter/    # Spring Boot starter (Java)
│   ├── REST API (reads via JobExplorer, acts via JobOperator/JobLauncher)
│   ├── Serves the compiled Angular UI at /batch-pilot
│   └── Auto-configuration; opt-in via one dependency + one property
└── batch-pilot-ui/         # Angular 18 + Material (standalone components)
```

- All read operations come from the **`BATCH_*` metadata tables your app already has** — no agents, no extra DB.
- All actions go through Spring Batch's own `JobOperator` — no bypassing framework semantics.
- The starter does nothing unless explicitly enabled.

## Quick start (target developer experience)

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

Open `http://localhost:8080/batch-pilot`. Done.

> **Until `0.1.0` is on Maven Central**, build from source first (next section);
> it then resolves from your local `~/.m2`.

## Build from source

```bash
git clone https://github.com/AdityaPatankar71/batch-pilot.git
cd batch-pilot
mvn clean install        # builds the Angular UI + starter, installs to ~/.m2
```

Then depend on the locally-installed build:

```xml
<dependency>
  <groupId>io.github.batchpilot</groupId>
  <artifactId>batch-pilot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Security model (non-negotiable defaults)

- Disabled unless `batch-pilot.enabled=true`.
- All endpoints require a configurable role (default `BATCH_PILOT_ADMIN`); integrates with the host app's existing Spring Security chain.
- Write actions (restart/launch/stop) individually toggleable (`batch-pilot.actions.launch=false` for view-only deployments).
- CSRF on, audit log on, no credentials stored.

## Configuration reference

All properties live under the `batch-pilot` prefix. The console contributes **no
beans at all** unless `enabled=true`.

| Property | Default | Description |
|---|---|---|
| `batch-pilot.enabled` | `false` | Master switch. When false, nothing is wired. |
| `batch-pilot.security.enabled` | `true` | When Spring Security is present, secure `/batch-pilot/**`. Set false to leave it to your own chain. |
| `batch-pilot.security.role` | `BATCH_PILOT_ADMIN` | Role required for the console. Maps to authority `ROLE_<role>`. |
| `batch-pilot.actions.restart` | `true` | Allow restarting executions. |
| `batch-pilot.actions.stop` | `true` | Allow stopping running executions. |
| `batch-pilot.actions.launch` | `true` | Allow launching jobs. |

For a **view-only** deployment, set all three `actions.*` to `false` — the write
buttons disappear from the UI and the action endpoints reject requests.

## REST API

The UI is a thin client over a JSON API rooted at `/batch-pilot/api`. Same
security and action toggles apply.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/jobs` | Registered jobs + last-execution summary. |
| `GET` | `/jobs/{jobName}/executions` | Execution history for a job. |
| `GET` | `/executions/{executionId}` | Execution detail: steps, counts, failure traces. |
| `GET` | `/actions` | Which write actions are enabled (drives the UI). |
| `POST` | `/executions/{executionId}/restart` | Restart a failed/stopped execution. |
| `POST` | `/executions/{executionId}/stop` | Request stop of a running execution. |
| `POST` | `/jobs/{jobName}/launch` | Launch a job with typed parameters. |
| `GET` | `/audit` | Action audit log. |

## Run the demo locally

```bash
mvn -pl batch-pilot-sample-app -am spring-boot:run
# open http://localhost:8080/batch-pilot
```

The sample app (H2, in-memory) registers `successJob`, `multiStepJob`,
`failingJob`, and `slowJob` so the console has real data on first start. Launch
`failingJob` for a stack trace, `slowJob` to watch a STARTED execution and exercise
stop. Demo logins are in `SampleSecurityConfig`.

## Comparison

| | Spring Batch Admin | Spring Cloud Data Flow | spring-batch-dashboard | Spring Boot Admin | **batch-pilot** |
|---|---|---|---|---|---|
| Maintained | ❌ archived 2019 | ⚠️ heavyweight, platform-scale | ✅ | ✅ | 🎯 |
| Drop-in (one dependency) | ❌ | ❌ own server | ✅ | ✅ | ✅ |
| Step-level failure detail | ✅ | ✅ | ✅ | ❌ | ✅ |
| Restart / launch / stop | ✅ | ✅ | ❌ read-only | ⚠️ minimal | ✅ |
| Built for batch operators | ✅ | ❌ data pipelines | ⚠️ viewer | ❌ | ✅ |

## Risks & mitigations

- *Spring Batch 5 API surface changes* → pin to Boot 3.x line; integration tests against two Boot minor versions.
- *"Why not SCDF?" objections* → answer in README (this comparison table) before anyone asks.
- *Security worries about write actions* → conservative defaults above; document threat model openly.

## Contributing / License

Apache-2.0 — see [LICENSE](LICENSE). Issues and PRs welcome; start with
[CONTRIBUTING.md](CONTRIBUTING.md) and the labeled
[good-first-issues](.github/GOOD_FIRST_ISSUES.md). Maintainers: see
[RELEASING.md](RELEASING.md) for the Maven Central publish flow.
