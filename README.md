# HR System

Developer Quickstart
- Compose: `docker compose up -d --build`
- App: http://localhost:8080 (Swagger: `/swagger-ui/index.html`)
- MySQL: host `3307` → container `3306`; DB `hr_system`
- Switch strategy: `APP_JOBDATA_QUERY_STRATEGY=jpa|jdbc` (default `jpa`)
- Profiles: `SPRING_PROFILES_ACTIVE=dev|prod` (default `dev`; Docker Compose sets `prod` by default)

Run locally (Windows)
- Dev: `$env:SPRING_PROFILES_ACTIVE='dev'; .\mvnw.cmd spring-boot:run`

API quick start (Swagger)
- Open: http://localhost:8080/swagger-ui/index.html

Data access (Strategy Pattern)
- JPA: readable, composable specs; great for evolving business logic.
- JDBC: lower overhead; full control over SQL & pagination; good for large, fast reads.
- Switch at runtime: `app.jobdata.query.strategy=jpa|jdbc` (env var: `APP_JOBDATA_QUERY_STRATEGY`).

Performance (JMH)
- Build: `.
mvnw.cmd -P jmh clean package`
- Run: `java -jar target/hr.system-1.0.0-benchmarks.jar com.swl.hr.system.benchmark.JobDataQueryStrategyBenchmark -rf csv -rff results.csv`
- Note: If exec:java cannot find `/META-INF/BenchmarkList`, prefer the shaded jar above.

Benchmarks (Summary)
- JDBC (sorted by title): avg ~1.57 ms/op; JPA: avg ~6.07 ms/op (JDBC ~3.9x faster)
- JDBC (salary range desc): avg ~0.092 ms/op; JPA: avg ~0.099 ms/op (similar)
- See `results.csv` for exact figures and confidence intervals.

Tech Highlights
- Global Exception Handling: `@RestControllerAdvice` centralizes API error responses.
- AOP Logging: cross-cutting request/response logging via `@Aspect` improves observability without polluting business logic.
- Strategy Pattern: pluggable JobData query implementations (`jpa` vs `jdbc`), switchable via `app.jobdata.query.strategy` or environment variable `APP_JOBDATA_QUERY_STRATEGY`.

Docker tips
- Start: `docker compose up -d --build`
- Logs: `docker compose logs -f app`
- Stop: `docker compose down`
- DB URLs:
    - Host→Compose MySQL: `jdbc:mysql://localhost:3307/hr_system`
    - App container→MySQL service: `jdbc:mysql://mysql:3306/hr_system`

Troubleshooting (short)
- Docker daemon: start Docker Desktop; `docker version` should show client & server.
- Port conflicts (8080/3307): free the port or adjust `docker-compose.yml`.
- JMH discovery error: run package first or use the shaded jar command above.
"# immigration-services-platform" 
