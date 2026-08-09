# Repository-specific instructions for spring-crud-base

This repository already contains a production-shaped Spring Boot 3 / Java 17 CRUD base for the Result domain. Use it as the reference implementation for any new CRUD feature.

## Default behavior
When the user asks to create, extend, or refactor a CRUD microservice in this repository, treat it as a one-shot implementation task:
- Do not ask for permission to scaffold.
- Do not split the work into multiple conversational rounds.
- Infer sensible defaults when the entity name or main fields are clear.

## What to implement
For a new CRUD capability, build the full stack in one pass:
1. Entity / JPA model
2. DTOs, validation, mapper
3. Repository and Specification-based filtering/pagination
4. Service + controller + error handling
5. OpenAPI annotations and docs
6. Tests (unit + Cucumber API acceptance)
7. Observability, Docker, and Allure wiring when the request implies a production-style delivery

## Repository conventions to preserve
- Keep the current package layout under src/main/java/com/example/crudbase
- Follow the existing Result-based pattern: Result, ResultRequestDTO, ResultResponseDTO, ResultFilter
- Use Spring Data JPA + Specifications for listing and filtering
- Keep endpoint naming consistent with /api/<entity>
- Preserve explicit bean wiring and the existing Spring Boot 3 / Java 17 stack
- Keep the contract and implementation aligned with the OpenAPI docs in docs/

## Verification expectations
Before finishing, verify the change with the relevant real command:
- mvn test for unit/service coverage
- mvn verify for API acceptance tests
- mvn spring-boot:run or mvn -DskipTests compile for a quick sanity check when appropriate

If the task is only to scaffold or extend the base service, prefer the spring-crud-builder agent or the implement-crud slash command and do the work in a single pass.
