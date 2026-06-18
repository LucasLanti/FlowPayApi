# FlowPay API

API do FlowPay feita com Spring Boot, Java, Maven, PostgreSQL e RabbitMQ.

## Requisitos

- Java 26
- Maven Wrapper incluido no projeto
- Docker e Docker Compose, caso queira subir banco, RabbitMQ e API por containers

## Configuracao

O projeto le configurações por variáveis de ambiente e possui valores padrão em `src/main/resources/application.yml`.

Principais variáveis:

```env
SERVER_PORT
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_RABBITMQ_HOST
SPRING_RABBITMQ_PORT
SPRING_RABBITMQ_USERNAME
SPRING_RABBITMQ_PASSWORD
```

O arquivo `.env` existente e usado pelo `docker-compose.yml`.

## Rodando com Docker Compose

Na raiz da API:
primeiro builde o projeto
```bash
./maven build
```
após a build suba o container docker da api, RabbitMQ e banco de dados
```bash
docker compose up --build
```

Servicos publicados por padrao:

```text
API: http://localhost:8081
Swagger UI: http://localhost:8081/swagger-ui.html
OpenAPI JSON: http://localhost:8081/v3/api-docs
PostgreSQL: localhost:5432
RabbitMQ: localhost:5672
RabbitMQ Management: http://localhost:15672
```

Credenciais padrao do RabbitMQ:

```text
guest / guest
```

## Rodando localmente

Suba PostgreSQL e RabbitMQ, depois execute:

```bash
./maven run
```

## Build

```bash
./maven build
```

## Testes

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```

O projeto usa Jacoco. Depois dos testes, o relatório fica em:

```text
target/site/jacoco/index.html
```

## Endpoints

A documentacao interativa tambem fica disponivel em:

```text
http://localhost:8081/swagger-ui.html
```

### Times

```http
GET /team/all
```

Retorna todos os times cadastrados.

### Tickets

```http
POST /ticket
```

Cria um ticket.

```json
{
  "teamId": "uuid-do-time",
  "content": "Descricao da solicitacao"
}
```

```http
GET /ticket/{teamId}?status=IN_PROGRESS&page=0&size=10
```

Lista tickets de um time. O filtro `status` e opcional.

```http
PATCH /ticket/{id}/finish
```

Finaliza um ticket.

### Atendentes

```http
POST /attendant
```

Cria um atendente.

```json
{
  "teamId": "uuid-do-time",
  "name": "Nome do atendente"
}
```

```http
GET /attendant/all?page=0&size=100
```

Lista atendentes cadastrados.

## Estrutura

```text
src/main/java/com/example/flowpay/configs       Configuracoes da aplicacao
src/main/java/com/example/flowpay/controllers   Controllers REST
src/main/java/com/example/flowpay/domains       Entidades de dominio
src/main/java/com/example/flowpay/dtos          DTOs de entrada e saida
src/main/java/com/example/flowpay/exceptions    Tratamento de erros
src/main/java/com/example/flowpay/repositories  Repositorios JPA
src/main/java/com/example/flowpay/services      Regras de negocio e filas
src/test/java/com/example/flowpay               Testes automatizados
```

## Observacoes

- O banco e inicializado com `src/main/resources/insert.sql`.
- O Hibernate esta configurado com `ddl-auto: update`.
- Tickets usam filas RabbitMQ por time para processamento.
