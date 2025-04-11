# Integração OAuth2 com HubSpot utilizando SpringBoot

Este projeto tem como finalidade implementar uma autenticação OAuth2 com a API da HubSpot para criação de Contatos.
Nele nós temos:
* Autenticação via código de autorização
* Armazenamento e atualização de token de forma automatica
* Integração com a API de contatos da HubSpot
* Webhook para eventos de criação de contatos da HubSpot



## As tecnologias utilizadas no projeto foram:

* Java 17+
* SpringBoot
* SpringWeb
* Spring Data JPA (h2)
* Jackson
* Lombok
* HubSpot API
* Docker
* Ngrok



### As funcionalidades presentes são:
- Autenticação OAuth2 com o HubSpot (Para segurança da API)
- Renovação de token de forma automatica quando expirado ( Automatização de fluxo)
- Cache de token em memória (melhoria de performace)
- Criação de contatos na HubSpot (cadastro de leads)
- Webhook para eventos (mapeamento da criação de contatos)



### Para rodar esse projeto você precisará:
- Java 17+
- Maven
- Ngrok
- Docker



## 🐳 Executando com Docker

### 🔧 Build da imagem Docker

```bash
docker build -t hubspot-oauth .
```

### ▶️ Executar o container

```bash
docker run -d -p 8080:8080 --name hubspot-app hubspot-oauth
```

A API estará disponível em `http://localhost:8080`.

### 🧼 Comandos úteis

```bash
docker stop hubspot-app
docker rm hubspot-app
docker build -t hubspot-oauth .
```


##  Melhorias planejadas

- Criar `docker-compose.yml` com suporte a banco externo (ex: PostgreSQL)
- Endpoint de gerenciamento de tokens (listar, validar, revogar, verificar scopes)
- Integração com frontend para autorização ( Apenas para um experiencia mais amigavel)
- Atualizar a URL de Webhook via API da HubSpot ( Hoje necessita atualizar no HubSpot toda vez que iniciar o Ngrok)

## Escolhas

| Biblioteca / Recurso | Finalidade | Motivo da escolha |
|----------------------|------------|--------------------|
| **Spring Boot**      | Framework principal para REST API | Produtivo, simples |
| **Java HttpClient**  | Comunicação com a API da HubSpot | Leve, sem necessidade de bibliotecas externas |
| **Jackson**          | Serialização / deserialização JSON | Já esta no embutido com Spring Boot |
| **Lombok**           | Redução de código repetitivo | Facilita construção de DTOs e logs |

