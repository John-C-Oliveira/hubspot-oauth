package com.example.oauth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

@Getter
@Schema(description = "Resposta com os dados do contato criado no HubSpot")
public class ContactResponseDTO {

    @Schema(description = "ID do contato", example = "113069054374")
    private String id;

    @Schema(description = "Propriedades do contato")
    private Map<String, String> properties;

    @Schema(description = "Data de criação do contato", example = "2025-04-10T22:58:49.543Z")
    private Date createdAt;

    @Schema(description = "Data de atualização do contato", example = "2025-04-10T22:58:49.543Z")
    private Date updatedAt;

    @Schema(description = "Indica se o contato foi arquivado", example = "false")
    private boolean archived;
}
