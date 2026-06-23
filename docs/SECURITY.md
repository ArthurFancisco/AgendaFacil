# Segurança

## Spring Security

O projeto usa Spring Security com login por formulário para o painel administrativo. Rotas públicas são limitadas a login, assets, erro e fluxo público de agenda em `/agenda/**`. Rotas `/panel/**` exigem autenticação.

Não remover Spring Security nem alterar a regra de autenticação sem revisão explícita.

## CSRF

CSRF permanece ativo pela configuração padrão do Spring Security. Formulários POST devem enviar o token CSRF gerado pelo Thymeleaf/Spring Security.

Não desabilitar CSRF para resolver erro de formulário; corrigir o formulário ou teste.

## BCrypt

Senhas administrativas são armazenadas como hash BCrypt. O `PasswordEncoder` configurado é `BCryptPasswordEncoder`.

## Senhas e variáveis de ambiente

Não salvar senha real no código, em documentação ou em migrations novas. Use variáveis de ambiente para configuração sensível:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

A migration inicial contém dados locais de demonstração. Antes de usar fora do ambiente local, trocar credenciais e seeds de demonstração por dados reais provisionados com processo seguro.

## Proteção contra acesso cruzado

O isolamento principal é por `establishmentId`.

- O painel obtém o estabelecimento pelo usuário autenticado.
- Operações administrativas buscam registros por `id + establishmentId`.
- Serviços, profissionais, bloqueios e agendamentos do painel não devem ser carregados apenas por `id`.
- A página pública resolve o estabelecimento pelo slug e usa esse `id` nas consultas seguintes.

Essa regra impede que o estabelecimento A aprove, cancele, liste ou altere dados do estabelecimento B.

## Controle por establishmentId

Toda regra que lê ou altera dados de negócio deve receber ou derivar o `establishmentId` confiável:

- no painel, do usuário autenticado;
- na agenda pública, do slug ativo;
- nunca de um campo livre enviado pelo cliente final.

## Ações administrativas

Ações críticas do painel continuam por POST e protegidas por CSRF. A criação manual, aprovação, recusa, cancelamento, falta e conclusão usam o estabelecimento do usuário autenticado e registram auditoria.

## Antifraude por telefone/IP

`BookingGuardService` registra tentativas de reserva por estabelecimento, telefone normalizado e IP. A regra atual bloqueia temporariamente:

- 3 ou mais tentativas para o mesmo telefone em 30 minutos;
- 10 ou mais tentativas para o mesmo IP em 30 minutos.

## Honeypot

O formulário público possui um campo honeypot. Se ele vier preenchido, a tentativa é tratada como suspeita e a reserva não é criada.

## Logs

Logs de suspeita registram estabelecimento e motivo, sem gravar nome, telefone completo ou outros dados sensíveis. Manter essa política em novas regras.

## Cliente final sem senha

O cliente final não precisa de senha porque não acessa um painel com dados privados. Ele apenas solicita agendamento por link público. A identificação operacional é feita por telefone normalizado + estabelecimento, e ações sensíveis continuam no painel autenticado do estabelecimento.
