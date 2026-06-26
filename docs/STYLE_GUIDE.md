# Guia de Estilo

Este guia registra a base visual do AgendaFacil Pro para a etapa de piloto. A interface deve parecer um SaaS operacional: clara, confiavel, responsiva e rapida de escanear.

## Paleta

Use os tokens de `src/main/resources/static/css/app.css` como fonte principal.

```css
:root {
  --bg: #F8FAFC;
  --surface: #FFFFFF;
  --surface-soft: #F1F5F9;
  --border: #E2E8F0;
  --text: #0F172A;
  --muted: #64748B;
  --primary: #2563EB;
  --primary-dark: #1D4ED8;
  --primary-soft: #DBEAFE;
  --accent: #14B8A6;
  --accent-soft: #CCFBF1;
  --success: #16A34A;
  --success-soft: #DCFCE7;
  --warning: #D97706;
  --warning-soft: #FEF3C7;
  --danger: #DC2626;
  --danger-soft: #FEE2E2;
  --shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}
```

## Componentes

Cards e paineis usam fundo branco, borda `--border`, raio curto e sombra leve. Evite blocos decorativos grandes; cada card deve existir para agrupar uma acao, uma tabela, uma metrica ou uma lista.

Botoes primarios usam azul (`--primary`) para a acao principal da tela. Botoes secundarios usam fundo branco com borda cinza. Acoes destrutivas usam vermelho apenas quando a acao altera estado de forma negativa, como cancelar ou sair.

Campos de formulario devem manter borda neutra e foco azul visivel. Mensagens de erro, sucesso e aviso usam as cores semanticas da paleta.

Use estes componentes CSS como base para novas telas:

- `.page-header`, `.page-title` e `.page-subtitle` para topo de paginas internas.
- `.admin-card`, `.metric-card`, `.summary-card` e `.empty-state` para conteudo.
- `.data-table` para tabelas operacionais.
- `.form-grid` e `.form-row` para formularios compactos.
- `.modal`, `.modal-header` e `.modal-actions` para formularios curtos.
- `.badge` e `.status-badge` para estados visuais.

## Status

Status de agendamento devem usar badges sem mostrar o enum cru:

- Confirmado: azul claro.
- Pendente: amarelo claro.
- Concluido: verde claro.
- Faltou: vermelho claro.
- Cancelado: cinza neutro.
- Expirado: cinza neutro.

## Pagina Publica

A jornada publica deve priorizar leitura em celular. O stepper fica compacto, com rolagem horizontal apenas em telas pequenas. A revisao antes de confirmar deve destacar servico, profissional, data, horario e dados do cliente sem criar conta para o cliente final.

## Painel

O painel deve favorecer densidade organizada: sidebar escura, metricas escaneaveis, filtros proximos da tabela e status visiveis. Evite textos explicativos longos dentro da tela; a interface deve ser direta para uso repetido.

A navegacao administrativa deve ficar separada por contexto:

- Dashboard: resumo, pendencias, agenda de hoje e atalhos.
- Agenda: visao semanal/diaria e filtros.
- Agendamentos: lista operacional e acoes por status.
- Clientes: busca, faltas e status.
- Servicos: catalogo e edicao de servicos.
- Profissionais: equipe e servicos vinculados.
- Bloqueios: periodos indisponiveis.
- Configuracoes: regras por grupos.
- Relatorios: indicadores simples.

O dashboard nao deve receber formularios grandes de cadastro. Cadastros simples podem abrir em modal; configuracoes extensas devem ficar em pagina propria.

## Modais

Use modal apenas para tarefas curtas, como criar ou editar servico, profissional, bloqueio ou agendamento manual. O modal deve ter titulo claro, texto curto, botao secundario para cancelar e botao primario para salvar. Acoes criticas continuam por POST com CSRF.

## Responsividade

Todas as telas principais precisam funcionar em largura de celular. Cards empilham, tabelas podem rolar horizontalmente e botoes importantes ocupam largura total apenas quando isso evita quebra de texto.

## Acessibilidade

Controles interativos devem ter foco visivel. Contraste entre texto e fundo deve ser suficiente para uso em ambientes reais de atendimento. Nao use cor como unico indicador quando houver decisao operacional importante.
