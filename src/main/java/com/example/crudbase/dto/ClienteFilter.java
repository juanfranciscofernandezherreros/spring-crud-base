package com.example.crudbase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional filters applied to the clients listing. Fields left unset are not filtered on.")
public class ClienteFilter {

    @Schema(description = "Exact client identifier", example = "1")
    private Long id;

    @Schema(description = "Partial, case-insensitive match on the first name", example = "Juan")
    private String firstName;

    @Schema(description = "Partial, case-insensitive match on the last name", example = "Fernandez")
    private String lastName;

    @Schema(description = "Partial, case-insensitive match on the email address", example = "juan.fernandez")
    private String email;

    @Schema(description = "Partial, case-insensitive match on the phone number", example = "600123")
    private String phone;

    @Schema(description = "Partial, case-insensitive match on the address", example = "Madrid")
    private String address;

    @Schema(description = "Exact creation timestamp", example = "2026-02-05T10:15:30Z")
    private Instant createdAt;
}
