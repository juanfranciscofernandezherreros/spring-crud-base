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

Run:

```bash
mvn test
```

Cucumber report: `target/cucumber-reports/cucumber.html`
