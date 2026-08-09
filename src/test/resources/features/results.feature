Feature: Manage results

  Background:
    Given the results database is empty

  Scenario: Get empty results collection
    When I request all results
    Then the response status should be 200
    And the response content type should be JSON
    And the response should contain an empty array

  Scenario: Get results collection with one record
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I request all results
    Then the response status should be 200
    And the response should contain 1 result
    And the response should contain a result with homeTeam "River Plate" and awayTeam "Boca Juniors"

  Scenario: Get results collection with multiple records
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    And a result exists with homeTeam "Racing", awayTeam "Independiente", homeScore 0, awayScore 0, matchDate "2026-01-11"
    When I request all results
    Then the response status should be 200
    And the response should contain 2 results

  Scenario: Get an existing result by id
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I request the result by its id
    Then the response status should be 200
    And the response should contain a result with homeTeam "River Plate" and awayTeam "Boca Juniors"

  Scenario: Get a result with an unknown id returns 404
    When I request the result with id 999999
    Then the response status should be 404
    And the error response should contain status 404, error "Not Found" and path "/api/results/999999"

  Scenario: Get a result with an invalid id format returns 400
    When I request the result with id "abc"
    Then the response status should be 400

  Scenario: Create a valid result
    When I create a result with homeTeam "Estudiantes", awayTeam "Gimnasia", homeScore 1, awayScore 1, matchDate "2026-02-05"
    Then the response status should be 201
    And the response should contain a generated id
    And the response should contain a result with homeTeam "Estudiantes" and awayTeam "Gimnasia"

  Scenario: Created result is persisted and retrievable afterward
    When I create a result with homeTeam "Talleres", awayTeam "Belgrano", homeScore 3, awayScore 0, matchDate "2026-02-06"
    And I request the result by its id
    Then the response status should be 200
    And the response should contain a result with homeTeam "Talleres" and awayTeam "Belgrano"

  Scenario Outline: Reject a result with missing or blank required fields
    When I create a result with homeTeam "<homeTeam>", awayTeam "<awayTeam>", homeScore <homeScore>, awayScore <awayScore>, matchDate "<matchDate>"
    Then the response status should be 400

    Examples:
      | homeTeam | awayTeam | homeScore | awayScore | matchDate  |
      |          | Boca     | 1         | 1         | 2026-02-05 |
      | River    |          | 1         | 1         | 2026-02-05 |

  Scenario: Reject a result with a negative score
    When I create a result with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore -1, awayScore 1, matchDate "2026-02-05"
    Then the response status should be 400

  Scenario: Reject a malformed JSON payload
    When I send a malformed JSON payload to create a result
    Then the response status should be 400

  Scenario: Fully update an existing result
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I update the result with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 5, awayScore 1, matchDate "2026-01-10"
    Then the response status should be 200
    And the response should contain a result with homeScore 5 and awayScore 1

  Scenario: Update is persisted
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I update the result with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 5, awayScore 1, matchDate "2026-01-10"
    And I request the result by its id
    Then the response should contain a result with homeScore 5 and awayScore 1

  Scenario: Update a result with an unknown id returns 404
    When I update the result with id 999999 using homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 1, awayScore 1, matchDate "2026-01-10"
    Then the response status should be 404

  Scenario: Update a result with an invalid payload returns 400
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I update the result with homeTeam "", awayTeam "Boca Juniors", homeScore 1, awayScore 1, matchDate "2026-01-10"
    Then the response status should be 400

  Scenario: Delete an existing result
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I delete the result
    Then the response status should be 204

  Scenario: Deleted result can no longer be retrieved
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I delete the result
    And I request the result by its id
    Then the response status should be 404

  Scenario: Delete a result with an unknown id returns 404
    When I delete the result with id 999999
    Then the response status should be 404

  Scenario: Repeated deletion of the same result returns 404
    Given a result exists with homeTeam "River Plate", awayTeam "Boca Juniors", homeScore 2, awayScore 1, matchDate "2026-01-10"
    When I delete the result
    And I delete the result
    Then the response status should be 404
