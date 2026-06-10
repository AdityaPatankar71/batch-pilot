# Screenshots & demo GIF

The README references these files. Capture them against the sample app and drop
them here (this directory is the only thing the README image links expect).

## Setup

```bash
mvn -pl batch-pilot-sample-app -am spring-boot:run
# open http://localhost:8080/batch-pilot
# log in with a demo user from SampleSecurityConfig
```

The sample app registers `successJob`, `multiStepJob`, `failingJob`, and `slowJob`
on startup, so the console has real data immediately. Run `failingJob` once
(Launch) to get a red FAILED row with a stack trace, and use `slowJob` to capture
a STARTED execution.

## Shot list

| File                    | What to show                                                                 |
|-------------------------|------------------------------------------------------------------------------|
| `jobs.png`              | Jobs overview table — mix of COMPLETED / FAILED / never-run statuses.        |
| `execution-detail.png`  | A `failingJob` execution: step table + expanded failure stack trace panel.   |
| `dark-mode.png`         | Any page with the toolbar dark-mode toggle on (proves the M3 dark theme).    |
| `demo.gif`              | The core loop: open a FAILED execution → click **Restart** → new execution.  |

## Tips

- Use a ~1280px-wide window for crisp tables.
- Toggle dark mode from the toolbar moon/sun icon for `dark-mode.png`.
- For `demo.gif`, keep it short (~8–12s): Jobs → failed execution → Restart →
  confirm dialog → success snackbar. Tools: macOS screen recording → convert with
  `ffmpeg`/`gifski`, or Kap.
- Keep each PNG well under a megabyte so the README loads fast.
