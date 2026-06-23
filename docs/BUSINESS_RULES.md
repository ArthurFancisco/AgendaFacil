# Regras de Negócio

## Cliente final

- Cliente final não cria conta.
- Cliente final não possui senha.
- Cliente é identificado por telefone normalizado + estabelecimento.
- Telefone não é chave primária.
- O banco usa `customers.id` como chave primária e uma restrição única em `establishment_id + phone_normalized`.
- O mesmo telefone pode existir em estabelecimentos diferentes como clientes separados.

## Telefone

- Máscaras são removidas antes de salvar.
- O DDI `55` é removido quando o número está no padrão DDI + DDD + celular.
- Telefones normalizados válidos têm 10 ou 11 dígitos.

## Status de agendamento

| Status | Label em português | Bloqueia horário? |
| --- | --- | --- |
| `PENDING_APPROVAL` | Pendente | Sim, enquanto não expirado |
| `CONFIRMED` | Confirmado | Sim |
| `COMPLETED` | Concluído | Não |
| `NO_SHOW` | Faltou | Não |
| `CANCELLED` | Cancelado | Não |
| `EXPIRED` | Expirado | Não |

## Status que bloqueiam horário

- `CONFIRMED`
- `PENDING_APPROVAL`

Uma pendência deixa de bloquear após expirar. O sistema expira pendências cujo `startAt` já passou.

## Status que liberam horário

- `CANCELLED`
- `NO_SHOW`
- `COMPLETED`
- `EXPIRED`

Recusar uma pendência altera o status para `CANCELLED` e libera o horário. Cancelar um agendamento confirmado ou pendente também libera o horário.

## Regra de conflito

Dois intervalos conflitam quando:

```text
novoInicio < agendamentoExistenteFim
E
novoFim > agendamentoExistenteInicio
```

Consequências:

- horário exatamente igual conflita;
- sobreposição parcial conflita;
- um horário que termina exatamente quando o próximo começa não conflita;
- um horário que começa exatamente quando o anterior termina não conflita.

## Faltas

- Marcar falta muda o agendamento de `CONFIRMED` para `NO_SHOW`.
- Marcar falta incrementa `Customer.noShowCount`.
- Cliente com 2 ou mais faltas pode solicitar agenda online, mas a nova reserva fica `PENDING_APPROVAL`.
- A base atual não bloqueia automaticamente ao chegar em 3 faltas; o bloqueio depende do campo `Customer.blocked`.

## Cliente bloqueado

- Cliente com `blocked = true` não consegue criar agendamento online.
- A validação ocorre após localizar o cliente por `establishmentId + phoneNormalized`.
- A mensagem orienta o cliente a falar com o estabelecimento pelo WhatsApp.

## Aprovação manual

Reservas entram como `PENDING_APPROVAL` quando:

- o cliente é novo naquele estabelecimento; ou
- o cliente já tem 2 ou mais faltas.

Reservas pendentes bloqueiam horário enquanto válidas. O estabelecimento pode aprovar, recusar ou cancelar.

## Liberação de horário

- `CONFIRMED` bloqueia horário.
- `PENDING_APPROVAL` bloqueia horário enquanto válido.
- Recusar libera horário.
- Cancelar libera horário.
- Expirar pendência libera horário.
- Concluir atendimento libera horário para regras futuras, mantendo histórico.
- Marcar falta libera horário para regras futuras, mantendo histórico.

## Agendamento manual pelo dono

- Agendamento manual só existe no painel autenticado.
- O dono informa nome, telefone, serviço, profissional, data, horário e observação interna opcional.
- O sistema normaliza o telefone e reutiliza cliente existente por `establishmentId + phoneNormalized`.
- Se não houver cliente, cria um cliente rápido.
- Agendamento manual sempre entra como `CONFIRMED`.
- Agendamento manual usa a mesma regra de conflito do agendamento público.
- Cliente bloqueado exige confirmação explícita do dono para criar manualmente.
- Cliente com 2 ou mais faltas deve ser apresentado com aviso humano ao dono.
- Criação manual não deve duplicar cliente com mesmo telefone no mesmo estabelecimento.
- Observação interna não aparece para o cliente final.

## Auditoria

O sistema registra auditoria para:

- criação manual;
- aprovação;
- recusa;
- cancelamento;
- falta;
- conclusão.
## Configuracoes por estabelecimento

Cada estabelecimento possui regras proprias em `establishment_settings`.

| Configuracao | Efeito |
| --- | --- |
| `newClientRequiresApproval` | Cliente novo entra como `PENDING_APPROVAL` quando ligado. |
| `pendingExpirationMinutes` | Tempo maximo, em minutos, para uma pendencia segurar horario. |
| `maxFutureAppointmentsPerPhone` | Limite de reservas futuras bloqueantes para o mesmo telefone no estabelecimento. |
| `maxAttemptsPerPhoneHour` | Limite de tentativas por telefone em 1 hora. |
| `maxAttemptsPerIpHour` | Limite de tentativas por IP em 1 hora. |
| `noShowCountForManualApproval` | A partir dessa quantidade de faltas, nova reserva publica fica `PENDING_APPROVAL`. |
| `noShowCountForBlock` | A partir dessa quantidade de faltas, o cliente nao agenda online. |
| `minHoursBeforeClientCancel` | Preparacao para cancelamento pelo cliente. |
| `longServiceManualApprovalMinutes` | Servicos acima dessa duracao ficam `PENDING_APPROVAL`. |
| `showPricesOnPublicPage` | Mostra ou esconde precos na pagina publica. |

As configuracoes nunca mudam a identidade do cliente: telefone normalizado + estabelecimento. Tambem nao mudam a regra de bloqueio de horario: apenas `CONFIRMED` e `PENDING_APPROVAL` ainda valido bloqueiam.
