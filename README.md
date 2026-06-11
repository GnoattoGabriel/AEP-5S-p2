# ConectaLocal — Marketplace de Serviços

Um trabalho por:
Sophia Machado Silva
RA:24087451-2
Gabriel de Oliveira Gnoatto
RA:23298801-2
Maria Eduarda Pereira Ribeiro
RA:24224683-2

Plataforma que conecta clientes a prestadores de serviço. Clientes solicitam serviços (catálogo ou post genérico), prestadores visualizam na job board, fazem claim e gerenciam na agenda.

## Funcionalidades

- Catálogo de serviços com busca por nome/categoria
- Solicitação de serviço com ou sem prestador definido
- Job board: prestadores veem solicitações disponíveis e podem assumir
- Agenda do prestador com atendimentos de hoje e próximos
- Acompanhamento de protocolo com atualização de status via HTMX
- Autenticação por email + role (CLIENTE/PRESTADOR)

## Tecnologias

- **Java 21**
- **Spring Boot 4.0.6** — Web, Security, Data JPA
- **Spring Security 7.0.5** — autenticação session-based
- **Thymeleaf** — templates server-side
- **HTMX 1.9.12** — interações dinâmicas sem JS pesado
- **Tailwind CSS** (CDN, sem build step)
- **H2 Database** — in-memory, auto-seed
- **Maven Wrapper** — build sem instalação prévia
- **Lombok** — reduz boilerplate

## Como compilar e rodar

```sh
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

Acessar em `https://conecta-local-jsdm.onrender.com` (host pode nao funcionar, rodar local é aconselhado).

em `http://localhost:8080/`. 

Console H2 em `/h2-console` — JDBC: `jdbc:h2:mem:servicosdb`.

## Usuários de teste

| Email | Senha | Role |
|---|---|---|
| `neymar.junior@gmail.com` | `senha123` | CLIENTE |
| `gabrielgnoatto@gmail.com` | `senha123` | PRESTADOR |
| `admin` | `admin` | CLIENTE ou PRESTADOR |
