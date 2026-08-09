# spring-crud-base

## Running the application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`. See [Docker](#docker) to run it (and the
observability stack) in containers instead.

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
- Exported OpenAPI specification: [docs/openapi.json](docs/openapi.json), [docs/openapi.yaml](docs/openapi.yaml)

### Listing, filtering and pagination

`GET /api/results` is paginated and filterable by every field of the entity:

```
GET /api/results?homeTeam=real&awayScore=1&page=0&size=20&sort=matchDate,desc
```

- `page`, `size`, `sort` control pagination (Spring Data `Pageable`); `size` defaults to 20, sorted by `id`.
- `id`, `homeScore`, `awayScore`, `matchDate` match exactly; `homeTeam`, `awayTeam`, `competition`, `venue`
  match partially and case-insensitively. All filters are optional and combine with AND.
- The response is a page envelope, not a bare array:

```json
{
  "content": [ { "id": 1, "homeTeam": "Real Madrid", "...": "..." } ],
  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
}
```

## API-First

`docs/openapi.yaml` (and its JSON equivalent) is the contract for the public API: every endpoint,
schema, validation rule, and error response documented in the running Swagger UI must match it. New
or changed endpoints are expected to update the contract, the controller annotations, the DTOs, and
the Cucumber scenarios together — if any of them disagree, the change isn't done.

The files under `docs/` are exported directly from the running application (`GET /v3/api-docs` and
`/v3/api-docs.yaml`) rather than hand-authored ahead of the implementation, since the API already
existed before this workflow was adopted. To regenerate them after a contract change:

```bash
mvn spring-boot:run
curl -s http://localhost:8080/v3/api-docs      -o docs/openapi.json
curl -s http://localhost:8080/v3/api-docs.yaml -o docs/openapi.yaml
```

Each operation has a stable `operationId` (`getAllResults`, `getResultById`, `createResult`,
`updateResult`, `deleteResult`) so client code generated from the contract doesn't churn when
descriptions change.

## Testing

The project has two test levels:

- **Service unit tests** (JUnit 5 + Mockito) run in the `test` phase via Surefire:
  ```bash
  mvn test
  ```
- **Cucumber API acceptance tests** run in the `integration-test`/`verify` phases via Failsafe:
  ```bash
  mvn verify
  ```

Cucumber runs through the classic JUnit4 `Cucumber` runner (`cucumber-junit`), forced onto the
`surefire-junit4` provider. This combination is the one that reliably fails the Maven build when a
scenario fails; running it under the default JUnit Platform provider (Surefire, alongside the JUnit 5
service tests) was found to silently report zero failures even when a scenario actually failed. Binding
it to Failsafe instead keeps both test levels correctly failing the build on any real failure, and
`mvn verify` runs both levels together.

Reports:

- Service unit tests (Surefire): `target/surefire-reports/`
- Cucumber acceptance tests (Failsafe): `target/failsafe-reports/`
- Cucumber HTML report: `target/cucumber-reports/cucumber.html`
- Cucumber JSON/XML reports: `target/cucumber-reports/cucumber.json`, `target/cucumber-reports/cucumber.xml`

### Allure

Every Cucumber scenario attaches its actual HTTP request and response (method, URL, headers, body —
sensitive headers/fields masked) to Allure, for both passed and failed scenarios. `mvn verify` (or
`mvn test-compile failsafe:integration-test`) produces the raw results; generating the HTML report is
a separate, explicit step:

```bash
mvn verify              # runs Cucumber, writes target/allure-results/
mvn allure:report       # renders target/allure-report/index.html from those results
```

Open `target/allure-report/index.html` directly in a browser. The first `allure:report` run downloads
a local Allure CLI + Node.js into `.allure/` at the project root (gitignored, ~180 MB) — subsequent
runs reuse it.

Allure does not replace the Cucumber HTML report; both are generated from the same test run.

## Docker

Prerequisites: Docker and Docker Compose.

```bash
cp .env.example .env      # first time only; edit values if needed
docker compose build
docker compose up -d
docker compose ps
docker compose logs -f app
docker compose down       # stop the stack, keep volumes
docker compose down -v    # stop the stack AND delete volume data (Prometheus/Loki/Grafana history)
```

This starts the application plus the full observability stack (Prometheus, Loki, Grafana Alloy,
Grafana) on a shared `crud-net` Docker network. Environment variables (see `.env.example`) control
host ports and Grafana's admin credentials; no secrets are committed to the repository.

The app image is a two-stage build (`maven:3.9-eclipse-temurin-17` to compile, `eclipse-temurin:17-jre`
to run), runs as a non-root user, and its `HEALTHCHECK` polls `/actuator/health`.

| Service    | URL                              | Notes                                   |
|------------|-----------------------------------|------------------------------------------|
| App        | http://localhost:8080             | Swagger UI at `/swagger-ui/index.html`   |
| Prometheus | http://localhost:9090             | Scrapes `app:8080/actuator/prometheus`   |
| Loki       | http://localhost:3100             | Queried through Grafana, not directly    |
| Grafana    | http://localhost:3000             | Login from `.env` (`admin` / `change-me` by default) |

Health endpoint: http://localhost:8080/actuator/health

The application uses an in-memory H2 database, so there is no separate database service or volume
to manage; `SPRING_PROFILES_ACTIVE=docker` (the container default) only disables the H2 console.

Tests still run the same way inside or outside Docker — `mvn clean verify` is the canonical build
and is what CI/the image build should run before producing a releasable artifact.

## Observability

Grafana, Prometheus and Loki are part of the same `docker compose up -d` stack described above.

- Grafana: http://localhost:3000 (datasources and the dashboard below are provisioned automatically —
  nothing to configure by hand)
- Prometheus: http://localhost:9090
- Application metrics endpoint: http://localhost:8080/actuator/prometheus

**Dashboard**: "Spring Boot Overview" (folder "Spring Boot") — requests/s, HTTP status distribution,
p95 latency, error rate, JVM heap/non-heap, CPU, live threads, GC pause time, Hikari connection pool,
application uptime, a browsable HTTP access log (every request, via Loki), and a ranked table of the
most requested endpoints over the selected time range (via Prometheus `topk`).

Useful PromQL:

```promql
sum(rate(http_server_requests_seconds_count{application="crud-base"}[5m]))
sum by (status) (rate(http_server_requests_seconds_count{application="crud-base"}[5m]))
histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{application="crud-base"}[5m])))
```

Useful LogQL (Grafana Explore, Loki datasource):

```logql
{container="crud-base-app"}
{container="crud-base-app"} |= "ERROR"
```

Application logs go to stdout and are collected by Grafana Alloy directly from the Docker socket
(read-only mount) and forwarded to Loki — no log files inside the app container.

Resetting local observability data (Prometheus/Loki/Grafana history) requires `docker compose down -v`;
everything else survives a normal `docker compose down`.

This is a local development stack: single-node Loki with filesystem storage, no auth on Grafana beyond
the admin password, and no alerting configured. None of that is production-ready as-is.
