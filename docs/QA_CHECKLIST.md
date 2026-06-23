# Checklist de QA

## Agenda pública

- [ ] Agendar em horário disponível cria reserva.
- [ ] Tentar agendar horário ocupado exibe mensagem amigável e não cria duplicidade.
- [ ] Reserva pendente bloqueia o horário enquanto válida.
- [ ] Reserva pendente expirada libera o horário.
- [ ] Cliente bloqueado não consegue agendar.
- [ ] Cliente novo entra como pendente.
- [ ] Cliente com 2 faltas entra como pendente.
- [ ] Página pública funciona em celular.
- [ ] Mensagens e badges exibem labels em português, sem enum cru.

## Painel

- [ ] Dono consegue criar novo agendamento manual.
- [ ] Agendamento manual em horário ocupado falha.
- [ ] Agendamento manual reutiliza cliente por telefone normalizado.
- [ ] Agendamento manual fica confirmado.
- [ ] Horário manual fica indisponível na página pública.
- [ ] Cliente com faltas exibe aviso humano.
- [ ] Cliente bloqueado exige confirmação explícita do dono.
- [ ] Cancelamento libera horário.
- [ ] Recusa de pendência libera horário.
- [ ] Aprovação revalida conflito antes de confirmar.
- [ ] Marcar falta incrementa contador do cliente.
- [ ] Concluir atendimento remove bloqueio futuro do horário sem apagar histórico.
- [ ] Estabelecimento A não acessa nem altera dados do estabelecimento B.

## Segurança

- [ ] POST autenticado no painel exige CSRF.
- [ ] POST público de confirmação exige CSRF.
- [ ] Rotas `/panel/**` exigem login.
- [ ] Rotas públicas de `/agenda/**` continuam acessíveis sem login.
- [ ] Logs de antifraude não expõem telefone completo, nome ou senha.

## Banco e configuração

- [ ] Flyway executa sem alterar migrations antigas.
- [ ] Aplicação sobe com credenciais via variáveis de ambiente.
- [ ] Nenhuma senha real é adicionada ao repositório.

## Testes automatizados mínimos

- [ ] `mvn clean test` passa em Java 17.
- [ ] PhoneNormalizer cobre máscara, sem máscara e inválido.
- [ ] Status bloqueantes e liberadores cobertos.
- [ ] Regra de conflito cobre igualdade, sobreposição e fronteiras sem conflito.
- [ ] Regra de faltas cobre incremento e exigência de aprovação manual.
## Configuracoes por estabelecimento

- [ ] Tela de configuracoes carrega no painel.
- [ ] Salvar configuracoes usa POST com CSRF.
- [ ] Configuracoes salvam para o estabelecimento autenticado.
- [ ] Cliente novo fica pendente quando a opcao estiver ligada.
- [ ] Cliente novo confirma automaticamente quando a opcao permitir.
- [ ] Limite de agendamentos futuros por telefone bloqueia novo horario.
- [ ] Limite de tentativas por telefone bloqueia excesso em 1 hora.
- [ ] Limite de tentativas por IP bloqueia excesso em 1 hora.
- [ ] Cliente com faltas entra como pendente conforme configuracao.
- [ ] Cliente com faltas acima do bloqueio nao agenda online.
- [ ] Servico longo entra como pendente conforme configuracao.
- [ ] Pendencia expirada por tempo libera horario.
- [ ] Precos aparecem ou somem na pagina publica conforme configuracao.
