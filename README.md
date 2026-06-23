# AgendaFacil Pro

Projeto em **Spring Boot 3 + Java 17** para agendamento online de pequenos estabelecimentos.

## Requisitos

- Java 17
- Maven
- Docker, para subir o PostgreSQL local

## Rodar localmente

Suba o banco:

```bash
docker compose up -d
```

Configure a senha do banco por variável de ambiente. No PowerShell:

```powershell
$env:DB_PASSWORD="agendafacil_dev"
mvn spring-boot:run
```

No Bash:

```bash
export DB_PASSWORD=agendafacil_dev
mvn spring-boot:run
```

Acesse:

- Público: `http://localhost:8080/agenda/bella-estetica`
- Painel: `http://localhost:8080/panel`

## Configuração

Variáveis aceitas:

- `DB_URL`, padrão `jdbc:postgresql://localhost:5432/agendafacil_pro`
- `DB_USERNAME`, padrão `agendafacil`
- `DB_PASSWORD`, sem valor padrão por segurança

## Observações

- O painel usa Spring Security e CSRF.
- Cliente final agenda sem criar conta.
- Flyway cria a base local e dados de demonstração.
- Não coloque senha real no código, no README ou em migrations.
