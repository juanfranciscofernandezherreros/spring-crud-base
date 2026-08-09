@api @results @crud
Feature: Results API

  Background:
    Given the results database is empty

  @smoke @positive
  Scenario: Get empty results collection
    When I send a GET request to "/api/results"
    Then the response status should be 200
    And the response should be an array

  @positive
  Scenario: Get results collection with one record
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a GET request to "/api/results"
    Then the response status should be 200
    And the response should contain 1 result

  @positive
  Scenario: Get results collection with multiple records
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    And a result exists with homeTeam "Racing", awayTeam "Independiente", homeScore 0, awayScore 0, matchDate "2026-01-11"
    When I send a GET request to "/api/results"
    Then the response status should be 200
    And the response should contain 2 results

  @positive
  Scenario: Get an existing result by id
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a GET request to "/api/results/{lastId}"
    Then the response status should be 200
    And the response field "homeTeam" should be "River Plate"
    And the response field "awayTeam" should be "Boca Juniors"

  @negative
  Scenario: Get an unknown result
    When I send a GET request to "/api/results/999999"
    Then the response status should be 404
    And the response field "status" should be 404
    And the response field "error" should be "Not Found"
    And the response field "path" should be "/api/results/999999"

  @negative @validation
  Scenario: Get a result with an invalid id format
    When I send a GET request to "/api/results/abc"
    Then the response status should be 400

  @smoke @positive
  Scenario: Create a valid result
    When I send a POST request to "/api/results" with body:
      """
      {
        "homeTeam": "Estudiantes",
        "awayTeam": "Gimnasia",
        "homeScore": 1,
        "awayScore": 1,
        "matchDate": "2026-02-05"
      }
      """
    Then the response status should be 201
    And the response field "id" should not be null
    And the response should contain:
      """
      {
        "homeTeam": "Estudiantes",
        "awayTeam": "Gimnasia",
        "homeScore": 1,
        "awayScore": 1,
        "matchDate": "2026-02-05"
      }
      """

  @positive
  Scenario: Created result is persisted and retrievable afterward
    When I send a POST request to "/api/results" with body:
      """
      {
        "homeTeam": "Talleres",
        "awayTeam": "Belgrano",
        "homeScore": 3,
        "awayScore": 0,
        "matchDate": "2026-02-06"
      }
      """
    And I send a GET request to "/api/results/{lastId}"
    Then the response status should be 200
    And the response field "homeTeam" should be "Talleres"
    And the response field "awayTeam" should be "Belgrano"

  @negative @validation
  Scenario Outline: Reject a result with missing or blank required fields
    When I send a POST request to "/api/results" with body:
      """
      {
        "homeTeam": "<homeTeam>",
        "awayTeam": "<awayTeam>",
        "homeScore": 1,
        "awayScore": 1,
        "matchDate": "2026-02-05"
      }
      """
    Then the response status should be 400

    Examples:
      | homeTeam | awayTeam |
      |          | Boca     |
      | River    |          |

  @negative @validation
  Scenario: Reject a result with a negative score
    When I send a POST request to "/api/results" with body:
      """
      {
        "homeTeam": "River Plate",
        "awayTeam": "Boca Juniors",
        "homeScore": -1,
        "awayScore": 1,
        "matchDate": "2026-02-05"
      }
      """
    Then the response status should be 400

  @negative
  Scenario: Reject a malformed JSON payload
    When I send a POST request to "/api/results" with body:
      """
      { "homeTeam": "River",
      """
    Then the response status should be 400

  @positive
  Scenario: Fully update an existing result
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a PUT request to "/api/results/{lastId}" with body:
      """
      {
        "homeTeam": "River Plate",
        "awayTeam": "Boca Juniors",
        "homeScore": 5,
        "awayScore": 1,
        "matchDate": "2026-01-10"
      }
      """
    Then the response status should be 200
    And the response field "homeScore" should be 5
    And the response field "awayScore" should be 1

  @positive
  Scenario: Update is persisted
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a PUT request to "/api/results/{lastId}" with body:
      """
      {
        "homeTeam": "River Plate",
        "awayTeam": "Boca Juniors",
        "homeScore": 5,
        "awayScore": 1,
        "matchDate": "2026-01-10"
      }
      """
    And I send a GET request to "/api/results/{lastId}"
    Then the response field "homeScore" should be 5
    And the response field "awayScore" should be 1

  @negative
  Scenario: Update a result with an unknown id
    When I send a PUT request to "/api/results/999999" with body:
      """
      {
        "homeTeam": "River Plate",
        "awayTeam": "Boca Juniors",
        "homeScore": 1,
        "awayScore": 1,
        "matchDate": "2026-01-10"
      }
      """
    Then the response status should be 404

  @negative @validation
  Scenario: Update a result with an invalid payload
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a PUT request to "/api/results/{lastId}" with body:
      """
      {
        "homeTeam": "",
        "awayTeam": "Boca Juniors",
        "homeScore": 1,
        "awayScore": 1,
        "matchDate": "2026-01-10"
      }
      """
    Then the response status should be 400

  @smoke @positive
  Scenario: Delete an existing result
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a DELETE request to "/api/results/{lastId}"
    Then the response status should be 204

  @positive
  Scenario: Deleted result can no longer be retrieved
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a DELETE request to "/api/results/{lastId}"
    And I send a GET request to "/api/results/{lastId}"
    Then the response status should be 404

  @negative
  Scenario: Delete a result with an unknown id
    When I send a DELETE request to "/api/results/999999"
    Then the response status should be 404

  @negative
  Scenario: Repeated deletion of the same result
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I send a DELETE request to "/api/results/{lastId}"
    And I send a DELETE request to "/api/results/{lastId}"
    Then the response status should be 404
