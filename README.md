# AgendaFácil Pro

Projeto completo em **Spring Boot 3 + Java 21** para agendamento online de pequenos estabelecimentos: salão, barbearia, estética, pet shop e clínicas simples.

## Rodar localmente

```bash
docker compose up -d
mvn spring-boot:run
```

Acesse:

- Público: `http://localhost:8080/agenda/bella-estetica`
- Painel: `http://localhost:8080/panel`
- Login demo local: `admin@demo.local`
- Senha demo local: `admin123`

## O que foi implementado

- Cliente agenda sem criar conta, usando nome e WhatsApp.
- Telefone é normalizado e vinculado ao estabelecimento.
- Dashboard protegido com Spring Security e CSRF.
- Aprovar, recusar, cancelar, concluir e marcar falta somente por POST.
- Conflito de horário validado no back-end.
- Pendentes e confirmados bloqueiam horário.
- Cancelados, recusados, faltas, concluídos e expirados ficam no histórico.
- Honeypot e limite simples de tentativas por telefone/IP.
- HTML e CSS com linguagem mais natural, visual leve e responsivo.
- Flyway com dados de demonstração.

## Importante

O usuário demo é apenas para ambiente local. Para produção, crie outro usuário com senha forte e remova os dados de demonstração.

## Observação para VS Code

Esta versão não usa Lombok. Os getters e setters foram escritos manualmente para evitar aqueles erros vermelhos do VS Code quando o plugin/annotation processor do Lombok não está configurado.

Se você já abriu a versão anterior, feche o VS Code, apague a pasta antiga, extraia este ZIP novo e abra a pasta do projeto novamente.
