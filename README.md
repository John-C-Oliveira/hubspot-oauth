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
- Ngrok (apenas se quiser testar o webhook)
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
- Integração com frontend para autorização (O fluxo de segurança foi feito apenas entre back-end e HubSpot. sem verificações com front ou BFF)
- Atualizar a URL de Webhook via API da HubSpot ( Hoje necessita atualizar no HubSpot toda vez que iniciar o Ngrok, apenas para esse projeto, visto que em produção o host é fixo)
- Adicionar uma verificação de cliente ao utilizar o token, visto que podemos ter mais de 1 cliente, então teriamos mais de 1 token. ( encaixa na parte de Integração com frontend abordada mais acima)
## Escolhas

| Biblioteca / Recurso | Finalidade                         | Motivo da escolha                              |
|----------------------|------------------------------------|------------------------------------------------|
| **Spring Boot**      | Framework principal para REST API  | Produtivo, simples                             |
| **Java HttpClient**  | Comunicação com a API da HubSpot   | Leve, sem necessidade de bibliotecas externas  |
| **Jackson**          | Serialização / deserialização JSON | Já esta no embutido com Spring Boot            |
| **Lombok**           | Redução de código repetitivo       | Facilita construção de DTOs e logs             |
| **NGrok**            | Testes locais para hosts remotos   | Necessidade de integração no teste com HubSpot |


