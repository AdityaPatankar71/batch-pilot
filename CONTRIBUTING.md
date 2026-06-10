# Contributing to batch-pilot

Thanks for helping build the operations console Spring Batch has been missing.
Issues and PRs are welcome. Look for the **`good first issue`** label if you're
new — see [`.github/GOOD_FIRST_ISSUES.md`](.github/GOOD_FIRST_ISSUES.md) for the
current starter set.

## Project layout

```
batch-pilot/
├── batch-pilot-starter/      # Spring Boot starter: REST API + auto-config (Java 17)
├── batch-pilot-ui/           # Angular 18 + Material console (served at /batch-pilot)
└── batch-pilot-sample-app/   # H2 demo app with success/multi-step/failing/slow jobs
```

## Building

A full Maven build compiles the Angular UI for you (via `frontend-maven-plugin`,
which downloads its own Node) and runs every module's tests:

```bash
mvn clean install
```

### Frontend-only loop

For fast UI iteration, run Angular directly against a running sample app:

```bash
# terminal 1 — backend with the console enabled
mvn -pl batch-pilot-sample-app -am spring-boot:run

# terminal 2 — Angular dev server with live reload
cd batch-pilot-ui && npm install && npm start
```

The dev server serves the console; point it at the sample app's API or proxy as
needed. The compiled production build is what the starter actually ships
(`npm run build:prod`).

### Running the demo

```bash
mvn -pl batch-pilot-sample-app -am spring-boot:run
# open http://localhost:8080/batch-pilot  (demo users are defined in SampleSecurityConfig)
```

## Coding conventions

- **Java**: standard Spring Boot style, Java 17. Keep the starter's auto-config
  opt-in and side-effect-free unless `batch-pilot.enabled=true`.
- **Angular**: standalone components, Angular Material, the `bp` selector prefix.
  Match the existing `loading` / `error` / empty-state pattern in each list
  component. Theme via the CSS variables and M3 themes in `src/styles.scss` — don't
  hard-code colors, so light and dark mode both stay correct.
- Keep new UI strings and surfaces accessible (labels on icon-only buttons).

## Pull requests

1. Branch off `main`.
2. Keep PRs focused — one issue per PR where possible.
3. Make sure `mvn clean install` passes (UI build + tests included).
4. Describe the change and link the issue it closes.

## License

By contributing you agree your contributions are licensed under the project's
[Apache-2.0](LICENSE) license.
