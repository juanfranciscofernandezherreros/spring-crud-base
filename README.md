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
