@api @clients @crud
Feature: Clients API

  Background:
    Given the clients database is empty

  @smoke @positive
  Scenario: Get empty clients collection
    When I send a GET request to "/api/clients"
    Then the response status should be 200
    And the response should be a page of clients
    And the response should contain 0 clients

  @positive
  Scenario: Get clients collection with one record
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a GET request to "/api/clients"
    Then the response status should be 200
    And the response should contain 1 client

  @positive
  Scenario: Get clients collection with multiple records
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients"
    Then the response status should be 200
    And the response should contain 2 clients

  @positive
  Scenario: Paginate clients with a page size smaller than the total
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    And a client exists with firstName "Carlos", lastName "Ruiz", email "carlos.ruiz@example.com", phone "+34600111222", address "Gran Via 3, Valencia"
    When I send a GET request to "/api/clients?page=0&size=2"
    Then the response status should be 200
    And the response should contain 2 clients
    And the total number of clients should be 3

  @positive
  Scenario: Filter clients by first name, partial and case-insensitive
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients?firstName=jua"
    Then the response status should be 200
    And the response should contain 1 client
    And the response field "content[0].firstName" should be "Juan"

  @positive
  Scenario: Filter clients by last name, partial and case-insensitive
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients?lastName=lopez"
    Then the response status should be 200
    And the response should contain 1 client
    And the response field "content[0].lastName" should be "Lopez"

  @positive
  Scenario: Filter clients by email, partial and case-insensitive
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients?email=MARIA.LOPEZ"
    Then the response status should be 200
    And the response should contain 1 client
    And the response field "content[0].email" should be "maria.lopez@example.com"

  @positive
  Scenario: Filter clients by phone, partial match
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients?phone=987654"
    Then the response status should be 200
    And the response should contain 1 client
    And the response field "content[0].firstName" should be "Maria"

  @positive
  Scenario: Filter clients by address, partial and case-insensitive
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients?address=barcelona"
    Then the response status should be 200
    And the response should contain 1 client
    And the response field "content[0].firstName" should be "Maria"

  @positive
  Scenario: Filter clients by multiple fields combined
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    And a client exists with firstName "Maria", lastName "Lopez", email "maria.lopez@example.com", phone "+34600987654", address "Avenida Diagonal 2, Barcelona"
    When I send a GET request to "/api/clients?firstName=Juan&lastName=Fernandez"
    Then the response status should be 200
    And the response should contain 1 client

  @negative
  Scenario: Filter clients with a value matching nothing
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a GET request to "/api/clients?firstName=Nonexistent"
    Then the response status should be 200
    And the response should contain 0 clients

  @positive
  Scenario: Get an existing client by id
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a GET request to "/api/clients/{lastId}"
    Then the response status should be 200
    And the response field "firstName" should be "Juan"
    And the response field "lastName" should be "Fernandez"

  @negative
  Scenario: Get an unknown client
    When I send a GET request to "/api/clients/999999"
    Then the response status should be 404
    And the response field "status" should be 404
    And the response field "error" should be "Not Found"
    And the response field "path" should be "/api/clients/999999"

  @negative @validation
  Scenario: Get a client with an invalid id format
    When I send a GET request to "/api/clients/abc"
    Then the response status should be 400

  @smoke @positive
  Scenario: Create a valid client
    When I send a POST request to "/api/clients" with body:
      """
      {
        "firstName": "Estela",
        "lastName": "Gimenez",
        "email": "estela.gimenez@example.com",
        "phone": "+34600555444",
        "address": "Plaza Espana 5, Sevilla"
      }
      """
    Then the response status should be 201
    And the response field "id" should not be null
    And the response should contain:
      """
      {
        "firstName": "Estela",
        "lastName": "Gimenez",
        "email": "estela.gimenez@example.com",
        "phone": "+34600555444",
        "address": "Plaza Espana 5, Sevilla"
      }
      """

  @positive
  Scenario: Created client is persisted and retrievable afterward
    When I send a POST request to "/api/clients" with body:
      """
      {
        "firstName": "Pablo",
        "lastName": "Torres",
        "email": "pablo.torres@example.com",
        "phone": "+34600222333",
        "address": "Calle Sol 8, Bilbao"
      }
      """
    And I send a GET request to "/api/clients/{lastId}"
    Then the response status should be 200
    And the response field "firstName" should be "Pablo"
    And the response field "lastName" should be "Torres"

  @negative @validation
  Scenario Outline: Reject a client with missing or blank required fields
    When I send a POST request to "/api/clients" with body:
      """
      {
        "firstName": "<firstName>",
        "lastName": "<lastName>",
        "email": "valid@example.com"
      }
      """
    Then the response status should be 400

    Examples:
      | firstName | lastName |
      |           | Fernandez |
      | Juan      |           |

  @negative @validation
  Scenario: Reject a client with an invalid email
    When I send a POST request to "/api/clients" with body:
      """
      {
        "firstName": "Juan",
        "lastName": "Fernandez",
        "email": "not-an-email"
      }
      """
    Then the response status should be 400

  @negative @validation
  Scenario: Reject a client with a blank email
    When I send a POST request to "/api/clients" with body:
      """
      {
        "firstName": "Juan",
        "lastName": "Fernandez",
        "email": ""
      }
      """
    Then the response status should be 400

  @negative
  Scenario: Reject a malformed JSON payload
    When I send a POST request to "/api/clients" with body:
      """
      { "firstName": "Juan",
      """
    Then the response status should be 400

  @positive
  Scenario: Fully update an existing client
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a PUT request to "/api/clients/{lastId}" with body:
      """
      {
        "firstName": "Juan",
        "lastName": "Fernandez",
        "email": "juan.new@example.com",
        "phone": "+34600999888",
        "address": "Calle Nueva 10, Madrid"
      }
      """
    Then the response status should be 200
    And the response field "email" should be "juan.new@example.com"
    And the response field "address" should be "Calle Nueva 10, Madrid"

  @positive
  Scenario: Update is persisted
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a PUT request to "/api/clients/{lastId}" with body:
      """
      {
        "firstName": "Juan",
        "lastName": "Fernandez",
        "email": "juan.new@example.com",
        "phone": "+34600999888",
        "address": "Calle Nueva 10, Madrid"
      }
      """
    And I send a GET request to "/api/clients/{lastId}"
    Then the response field "email" should be "juan.new@example.com"
    And the response field "address" should be "Calle Nueva 10, Madrid"

  @negative
  Scenario: Update a client with an unknown id
    When I send a PUT request to "/api/clients/999999" with body:
      """
      {
        "firstName": "Juan",
        "lastName": "Fernandez",
        "email": "juan.fernandez@example.com"
      }
      """
    Then the response status should be 404

  @negative @validation
  Scenario: Update a client with an invalid payload
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a PUT request to "/api/clients/{lastId}" with body:
      """
      {
        "firstName": "",
        "lastName": "Fernandez",
        "email": "juan.fernandez@example.com"
      }
      """
    Then the response status should be 400

  @smoke @positive
  Scenario: Delete an existing client
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a DELETE request to "/api/clients/{lastId}"
    Then the response status should be 204

  @positive
  Scenario: Deleted client can no longer be retrieved
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a DELETE request to "/api/clients/{lastId}"
    And I send a GET request to "/api/clients/{lastId}"
    Then the response status should be 404

  @negative
  Scenario: Delete a client with an unknown id
    When I send a DELETE request to "/api/clients/999999"
    Then the response status should be 404

  @negative
  Scenario: Repeated deletion of the same client
    Given a client exists with firstName "Juan", lastName "Fernandez", email "juan.fernandez@example.com", phone "+34600123456", address "Calle Mayor 1, Madrid"
    When I send a DELETE request to "/api/clients/{lastId}"
    And I send a DELETE request to "/api/clients/{lastId}"
    Then the response status should be 404
