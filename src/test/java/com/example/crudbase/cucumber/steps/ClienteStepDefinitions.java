package com.example.crudbase.cucumber.steps;

import com.example.crudbase.model.Cliente;
import com.example.crudbase.repository.ClienteRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ClienteStepDefinitions {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CommonStepDefinitions common;

    @Given("the clients database is empty")
    public void theClientsDatabaseIsEmpty() {
        clienteRepository.deleteAll();
        common.setLastCreatedId(null);
    }

    @Given("a client exists with firstName {string}, lastName {string}, email {string}, phone {string}, address {string}")
    public void aClientExistsWith(String firstName, String lastName, String email, String phone, String address) {
        Cliente cliente = Cliente.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .address(address)
                .build();
        Cliente saved = clienteRepository.save(cliente);
        common.setLastCreatedId(saved.getId());
    }

    @Then("the response should be a page of clients")
    public void theResponseShouldBeAPageOfClients() {
        assertThat(common.getResponse().jsonPath().getList("content")).as("content").isNotNull();
    }

    @Then("the response should contain {int} client(s)")
    public void theResponseShouldContainNClients(int count) {
        assertThat(common.getResponse().jsonPath().getList("content")).hasSize(count);
    }

    @Then("the total number of clients should be {int}")
    public void theTotalNumberOfClientsShouldBe(int total) {
        assertThat(common.getResponse().jsonPath().getLong("page.totalElements")).isEqualTo(total);
    }
}
