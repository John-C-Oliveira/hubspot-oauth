package com.example.oauth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequestDTO {

    @Schema(description = "Endereço de e-mail do cContact", example = "john@gmail.com")
    private String email;

    @Schema(description = "Primeiro nome do contato", example = "John")
    private String firstname;

    @Schema(description = "Sobrenome do contato", example = "Oliveira")
    private String lastname;

    @Schema(description = "Telefone do contato", example = "(84) 99999-9999")
    private String phone;
}
