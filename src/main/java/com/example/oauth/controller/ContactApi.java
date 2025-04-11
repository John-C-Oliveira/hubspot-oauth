package com.example.oauth.controller;

import com.example.oauth.model.ContactRequestDTO;
import com.example.oauth.model.ContactResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Contact", description = "Operações relacionadas à criação de contatos no HubSpot")
@RequestMapping("/management")
public interface ContactApi {

    @Operation(
            summary = "Cria um novo contato no HubSpot",
            description = "Recebe os dados do contato (email, primeiro nome, ultimo nome) e envia para o HubSpot."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Contato criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ContactResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao criar o contato",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    @PostMapping("/create-contact")
    ResponseEntity<String> createContact(
            @Parameter(description = "Dados do contato", required = true)
            @RequestBody ContactRequestDTO contactRequest
    );
}
