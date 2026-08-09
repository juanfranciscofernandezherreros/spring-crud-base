# spring-crud-base

## Running the application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
- Exported OpenAPI specification: [docs/openapi.json](docs/openapi.json), [docs/openapi.yaml](docs/openapi.yaml)

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
