# Good first issues (seed set)

Five small, self-contained tasks to file with the **`good first issue`** label
once the repo is public. Each names the file(s) to touch and mirrors a pattern
already in the codebase, so a newcomer can ship without a deep tour.

---

## 1. Add a relative-time pipe ("2 hours ago") to the lists

**Label:** `good first issue` · `ui` · `enhancement`

Timestamps in the jobs and executions tables show only an absolute date
(`{{ ... | date: 'medium' }}`). Add a relative time alongside it so operators can
scan "what ran recently" at a glance.

- Create `batch-pilot-ui/src/app/relative-time.pipe.ts`, mirroring the existing
  `duration.pipe.ts` (standalone, pure pipe).
- Output e.g. `just now`, `5 min ago`, `3 h ago`, `2 d ago`; fall back to the
  absolute date past ~7 days.
- Use it in `jobs-list.component.ts` (Last run) and `executions-list.component.ts`
  (Started), e.g. as a tooltip or secondary line next to the absolute date.

**Acceptance:** relative time renders in both tables; `mvn -pl batch-pilot-ui install`
(or `npm run build:prod`) passes.

---

## 2. Copy-to-clipboard button on failure stack traces

**Label:** `good first issue` · `ui` · `enhancement`

In `execution-detail.component.ts`, each failure panel renders a stack trace in a
`<pre class="bp-stacktrace">`. Add a small `mat-icon-button` (icon `content_copy`)
in the panel header that copies that trace via `navigator.clipboard.writeText`,
with a `MatSnackBar` "Copied" confirmation (snackbar is already imported).

**Acceptance:** clicking copy places the full trace on the clipboard and shows the
confirmation; the button has an `aria-label`.

---

## 3. Filter the executions list by status

**Label:** `good first issue` · `ui` · `enhancement`

`executions-list.component.ts` shows every execution for a job. Add a client-side
status filter (a `mat-button-toggle-group` or `mat-select`: All / COMPLETED /
FAILED / STARTED / STOPPED) above the table that filters the rendered rows. No API
change — filter the already-loaded `executions` array.

**Acceptance:** selecting a status narrows the table; "All" restores it; the empty
state shows sensibly when a filter matches nothing.

---

## 4. Auto-refresh an in-progress execution

**Label:** `good first issue` · `ui` · `enhancement`

When viewing a STARTED/STARTING execution (the sample app's `slowJob` is perfect
for testing), the detail page is static until you reload. In
`execution-detail.component.ts`, poll `getExecution` on an interval (e.g. every 2s,
RxJS `interval` + `takeWhile`) while the status is running, and stop polling once
it reaches a terminal state. Clean up the subscription in `ngOnDestroy`.

**Acceptance:** launch `slowJob`, open its detail page, and watch step counts /
status update without a manual reload; polling stops when the job finishes.

---

## 5. Per-route page titles

**Label:** `good first issue` · `ui` · `good-housekeeping`

The browser tab always reads "batch-pilot". Set a descriptive `<title>` per route
(e.g. `batch-pilot · Jobs`, `batch-pilot · Execution #42`) using Angular's `Title`
service or the router's `title` resolver in `app.routes.ts`.

**Acceptance:** navigating between Jobs / Executions / Execution detail / Audit
updates the browser tab title accordingly.
