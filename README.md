# Feature Toggle Portal with CI Quality and Security Gate

Spring Boot portal where an admin turns modules and features on or off at runtime (no redeploy).
Every pull request must pass a four-check gate before it can merge:

1. Type-check (compiler warnings are errors)
2. Lint (Checkstyle)
3. Unit tests with an enforced coverage minimum (JaCoCo, 70%)
4. Dependency vulnerability scan (Trivy)

See [docs/CI-GATE.md](docs/CI-GATE.md) for details and the demo script.

## Run locally

```
./mvnw spring-boot:run
```

Open http://localhost:8082. Demo logins: `admin@test.com` / `admin123`, `farmer@test.com` / `farmer123`.
