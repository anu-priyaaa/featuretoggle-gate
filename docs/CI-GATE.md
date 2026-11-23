# CI quality and security gate

Every pull request into `main` must pass four checks. If one fails, the PR cannot be merged.

| # | Check | Tool | Fails when |
|---|-------|------|-----------|
| 1 | Type-check | `javac` via Maven (`-Xlint:all -Werror`) | code does not compile, or any compiler warning |
| 2 | Lint | Checkstyle (`config/checkstyle.xml`) | any rule violation |
| 3 | Unit tests + coverage | JUnit + JaCoCo | a test fails, or line coverage < `coverage.minimum` (pom.xml, now 70%) |
| 4 | Dependency scan | Trivy | HIGH or CRITICAL vulnerability with a fix available |

Workflow: `.github/workflows/pr-gate.yml`

## Run the same gate locally

```
./mvnw clean verify checkstyle:check
```

## One-time GitHub setup (make the gate enforce, not advise)

1. Push the repo to GitHub with `main` as the default branch.
2. Settings > Branches > Add rule for `main`:
   - Require a pull request before merging
   - Require status checks to pass: select all four jobs
     (`1 - Type-check`, `2 - Lint`, `3 - Unit tests and coverage`, `4 - Dependency vulnerability scan`)
   - Require branches to be up to date
   - Do not allow bypassing the above settings
3. The job names only appear in the picker after the workflow has run once.

## Demo script: red, then green

| Branch | Change | Expected |
|--------|--------|----------|
| `demo/failing-test` | break an assertion in `FeatureFlagServiceTest` | check 3 red |
| `demo/low-coverage` | delete `AccessServiceTest.java` | check 3 red (coverage below 70%) |
| `demo/lint` | add an unused import to any class | check 2 red |
| `demo/vulnerable-dependency` | add an old library, e.g. `org.apache.logging.log4j:log4j-core:2.14.1` to pom.xml | check 4 red |

Fix the change, push again, and the same PR turns green and can be merged.

## Feature flags and the gate

Unfinished work is merged behind a feature or module toggle (off by default). The admin switches it on at
runtime, no redeploy. The gate keeps `main` safe while that unfinished code lands.
