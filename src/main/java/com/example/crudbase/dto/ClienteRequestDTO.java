package com.example.crudbase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data required to create or update a client")
public class ClienteRequestDTO {

    @Schema(description = "Client's first name", example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "firstName must not be blank")
    private String firstName;

    @Schema(description = "Client's last name", example = "Fernandez", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "lastName must not be blank")
    private String lastName;

    @Schema(description = "Client's email address", example = "juan.fernandez@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a well-formed email address")
    private String email;

    @Schema(description = "Client's contact phone number", example = "+34600123456")
    @Pattern(regexp = "^[0-9+()\\-\\s]{0,20}$", message = "phone must be a valid phone number")
    private String phone;

    @Schema(description = "Client's postal address", example = "Calle Mayor 1, Madrid")
    @Size(max = 255, message = "address must not exceed 255 characters")
    private String address;
}
