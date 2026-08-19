# Trevo — Estado do projeto

Rastreia o status de cada requisito de `Docs/Trevo - Requisitos.dc.html`. Atualizado a cada `/nova-rf` (passo 7). Status possíveis: `não iniciado` · `em andamento` · `bloqueado` · `concluído`.

Legenda de prioridade (MoSCoW): **M** obrigatório na v1 · **S** importante mas adiável · **C** desejável.
Legenda de fase: **F1** MVP publicável · **F2** Retenção · **F3** Monetização · **F4** Escala.

## RF-01 · Cadastro e onboarding

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-01.1 | Tela inicial com proposta + aviso de aleatoriedade | M | F1 | concluído |  |
| RF-01.2 | Coletar nome e data de nascimento em campos separados | M | F1 | concluído |  |
| RF-01.3 | Validar data dd/mm/aaaa (mês, dia, bissexto, ano 1900–hoje) | M | F1 | concluído |  |
| RF-01.4 | Bloquear cadastro de menores de 18 anos | M | F1 | concluído |  |
| RF-01.5 | Calcular signo a partir da data válida; marcador neutro se inválida | M | F1 | concluído |  |
| RF-01.6 | Impedir avanço do passo com erro de validação | M | F1 | concluído | |
| RF-01.7 | Apresentar as 12 crenças, seleção múltipla | M | F1 | concluído | |
| RF-01.8 | Limitar seleção a 3 crenças no grátis, cadeado leva ao paywall | M | F3 | em andamento | limite de 3 e cadeado prontos; toque no cadeado ainda não leva a lugar nenhum (RF-09/paywall não existe) |
| RF-01.9 | Auto-formatar data de nascimento com barras enquanto digita | C | F1 | concluído |  |

## RF-02 · Motor de geração de palpites

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-02.1 | Gerar volante de 15 dezenas (1–25, sem repetição) por sorteio ponderado | M | F1 | concluído | |
| RF-02.2 | Somar peso por crença ativa; dezenas fixas com peso dominante | M | F1 | concluído | |
| RF-02.3 | Implementar as 12 crenças | M | F1 | concluído | |
| RF-02.4 | Traduzir sonho pelos 25 grupos do jogo do bicho (grupo N + espelho 26−N) | M | F1 | concluído | |
| RF-02.5 | Excluir sem falhar crenças com dado de origem ausente/inválido | M | F1 | concluído | |
| RF-02.6 | Registrar qual crença contribuiu com quais dezenas | M | F1 | concluído | |
| RF-02.7 | Calcular índice de força do palpite | S | F1 | concluído | |
| RF-02.8 | Gerar fechamentos de 16, 18 e 20 dezenas | S | F3 | concluído | `PalpiteGenerator.gerarFechamento` + `TamanhoDeFechamento`; sem tela própria — quem consome isso é RF-04 (desdobramentos), ainda não iniciado |
| RF-02.9 | Animação de ritual na geração (≥3 frases) | C | F1 | concluído | `TelaGerando` (wireframe 1f), 4 frases, entre Crenças e Home; respeita preferência de movimento reduzido do sistema |

## RF-03 · Home e gestão de palpites

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-03.1 | Concurso corrente, horário de fechamento (19h) e sorteio (20h) | M | F1 | em andamento | horário corrigido pra 20h/21h (`TelaHome`) — o resumo "(19h)/(20h)" no próprio texto do requisito está errado contra `Docs/tabelavalores.md` (fonte oficial: apostas até 20h, sorteio às 21h, seg-sáb); avise pra corrigir o requisito. Número do concurso corrente ainda não aparece na Home — RF-05 já traz o número real via API, mas a Home em si não foi conectada a isso nesta fatia; CLAUDE.md §8 proíbe inventar dado de sorteio, então isso só é exibível com o resultado já buscado |
| RF-03.2 | Índice de sorte do dia, fase da lua, signo | S | F1 | concluído | índice de sorte é fórmula decorativa nova (sem base em nenhum doc — protótipo só tinha valor mockado); lua/signo usam cálculo já existente do RF-02 |
| RF-03.3 | Seletor dos 25 grupos do jogo do bicho quando sonho ativo | M | F1 | concluído | prévia de 4 + expansão inline pros 25; gated por `Crenca.SONHO` no perfil salvo |
| RF-03.4 | Listar palpites do dia com dezenas, horário, força, crenças | M | F1 | concluído | crenças usadas por palpite ainda não aparecem no card, só dezenas/horário/força |
| RF-03.5 | Indicar dezenas que mudaram vs. palpite anterior | S | F1 | concluído | |
| RF-03.6 | Total de jogos do dia e custo na lotérica | M | F1 | concluído | |
| RF-03.7 | Estado vazio sem palpites | M | F1 | concluído | |
| RF-03.8 | Excluir palpite com confirmação (lista e detalhe) | M | F1 | em andamento | confirmação pronta na lista (`TelaHome`); não há tela de detalhe ainda (RF-04) |
| RF-03.9 | Exibir palpites restantes no dia / ilimitado no Pro | M | F3 | não iniciado | |
| RF-03.10 | Tocar grupo abre card com nome, número, leitura, dezenas | M | F1 | concluído | `DialogoCartaoDoSonho`, wireframe 1t |
| RF-03.11 | Leitura escrita pros 25 grupos, tradição popular | M | F1 | concluído | conteúdo portado do protótipo de referência (`GRUPOS_DO_BICHO`) |
| RF-03.12 | Confirmar grupo como sonho do dia, indicar já escolhido | M | F1 | em andamento | confirmação e persistência (DataStore) prontas; ainda não alimenta uma geração real porque a Home não tem botão de gerar (RF-11 não existe) |
| RF-03.13 | Card informa que a leitura não altera a probabilidade | M | F1 | concluído | |

## RF-04 · Detalhe, edição e desdobramentos

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-04.1 | Volante em grade 5×5, marcadas distinguíveis | M | F1 | concluído | `GradeDeDezenas` (novo, reutilizado em RF-04.5) |
| — | Barra de navegação inferior (Início/Conferir/Histórico/Perfil) — achado da auditoria de fidelidade contra os wireframes/protótipo (RF-05) | — | — | concluído | `BarraDeNavegacaoInferior` (`:core:ui`, usa `NavigationBar`/`NavigationBarItem` do Material 3) + `BarraDeNavegacaoDoApp` (`:app`, ícones Phosphor via `com.adamglin:phosphor-icon`). Estado ativo por `NavController.currentBackStackEntryAsState()` em `TrevoNavHost` (Scaffold), não por flag manual em cada tela. Aparece em Home/Detalhe/Desdobramentos/Conferência; ausente em onboarding, ritual/geração e modais — segue exatamente o `abas` do protótipo (`Trevo - Lotofácil.dc.html`, ~linha 2161). Histórico/Perfil ficam visíveis mas inertes até RF-06/RF-07 existirem |
| RF-04.2 | Listar crença + explicação + dezenas que ela trouxe | M | F1 | concluído | explicação reaproveita as strings estáticas `crenca_X_desc` (já usadas em `TelaCrencas`) em vez de persistir a explicação dinâmica de `ContribuicaoDeCrenca` — evita migração do Room; ver nota no plano da sessão |
| RF-04.3 | Estatísticas: soma, pares/ímpares, moldura/miolo, custo | S | F1 | concluído | moldura/miolo reaproveita `DEZENAS_DA_MOLDURA` de `FonteMoldura.kt` |
| RF-04.4 | Probabilidade real de 15 acertos do fechamento escolhido | M | F1 | concluído | `probabilidadeDe15Acertos` (novo, `:core:engine`) |
| RF-04.5 | Editar manualmente as dezenas na grade | M | F1 | concluído | |
| RF-04.6 | Bloquear salvar edição com contagem divergente do fechamento | M | F1 | concluído | |
| RF-04.7 | Guardar dezenas manuais como fixas permanentes; exibir e limpar | M | F1 | concluído | |
| RF-04.8 | Refazer o palpite mantendo crenças e fixas | M | F1 | concluído | |
| RF-04.9 | Qtde de jogos de 15 equivalentes + custo total (fechamento >15) | S | F3 | concluído | `coeficienteBinomial` bate exatamente com `Docs/tabelavalores.md` pros 6 tamanhos oficiais |
| RF-04.10 | Listar combinações do desdobramento (limitado em tela) | S | F3 | concluído | `combinacoesDe15` (sequence preguiçosa) + `TelaDesdobramentos`, limite de 24. Hoje nenhum palpite real passa de 15 dezenas (Home/Crenças sempre geram 15 — o seletor de fechamento de RF-04 mostra 16/18/20 com 🔒, que é RF-09/Pro, ainda não existe), então a tela é alcançável só via dados de teste até RF-09/RF-11 existirem |

## RF-05 · Resultados e conferência

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-05.1 | Buscar resultado oficial na API da Caixa | M | F1 | concluído | `ResultadoApi`/`ResultadoRepositoryImpl` (`:core:data`), API real da Caixa (`servicebus2.caixa.gov.br`), verificado com chamada de rede real no emulador |
| RF-05.2 | Armazenar resultados localmente (consulta offline) | M | F1 | concluído | tabela `resultados` (Room, migração `MIGRATION_1_2` versão 1→2, testada com `MigrationTestHelper`) |
| RF-05.3 | Conferir automaticamente todos os palpites do concurso | M | F2 | concluído | roda ao entrar na `TelaConferencia` (`ConferenciaViewModel.aoEntrar`), não em background/WorkManager — ver pendência de RNF-02.3 |
| RF-05.4 | Exibir acertos, faixa premiada e valor do prêmio por palpite | M | F1 | concluído | `conferir()` (`:core:engine`) + `TelaConferencia` |
| RF-05.5 | Destacar no volante: acerto, marcada não sorteada, sorteada não marcada | M | F1 | concluído | `estadosDasDezenas()` (`:core:engine`, as 25 dezenas); a tela de Conferência em si usa o resumo compacto por palpite do wireframe 1j (cheia=acerto/tracejada=miss), igual ao protótipo — a grade 5×5 completa com os 3 estados fica reservada pra quando `TelaDetalhe` ganhar essa integração |
| RF-05.6 | Total ganho e total gasto no concurso | M | F1 | concluído | `ConferenciaUiState.Sucesso.totalGanho/totalGasto` |
| RF-05.7 | Estado de espera (concurso ainda não sorteado) | M | F1 | concluído | compara `dataApuracao` do resultado mais recente salvo com a data de criação dos palpites de hoje — nunca calcula número de concurso offline; verificado com chamada real (resultado do concurso anterior, hoje ainda não sorteado) |
| RF-05.8 | Estado offline, com aviso de que a conferência ocorrerá depois | M | F2 | concluído | `IOException` → `ConferenciaUiState.SemConexao` |
| RF-05.9 | Estado de falha do serviço com nova tentativa | M | F2 | concluído | qualquer outra exceção → `ConferenciaUiState.Falha`; botão "Tentar de novo" chama `aoTentarNovamente()` |
| RF-05.10 | Entrada manual das dezenas sorteadas (fallback) | C | F2 | concluído | diálogo com `GradeDeDezenas` em modo seleção (15 dezenas), acessível a partir de `SemConexao`/`Falha`; resultado manual não tem `faixasDePremio` (nunca inventa valor de prêmio) |

## RF-06 · Histórico

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-06.1 | Listar concursos conferidos, mais recente primeiro, com palpites | M | F2 | concluído | `HistoricoViewModel` casa cada dia com palpites (`observarTodosOsPalpites`) contra todo resultado já buscado (`observarTodosOsResultados` — a tabela `resultados` só acumula, nunca substitui, então já é um histórico real desde o RF-05); dia sem resultado casado fica de fora — nunca inventa a associação (CLAUDE.md §8) |
| RF-06.2 | Total gasto/ganho, saldo, retorno %, média por concurso | M | F2 | concluído | custo real por palpite (`coeficienteBinomial` + `CUSTO_POR_JOGO`, não um valor fixo por jogo); saldo negativo exibido sem eufemismo (RF-10.4), verificado no emulador |
| RF-06.3 | Distribuição de faixas 11–15 e melhor resultado | S | F2 | concluído | |
| RF-06.4 | Paginação incremental ("carregar mais") | M | F2 | concluído | revela 3 concursos por vez, igual ao protótipo — client-side sobre a lista local (todos os concursos já vêm do Room), não paginação de rede |
| RF-06.5 | Estado vazio sem concurso conferido | M | F2 | concluído | |
| RF-06.6 | Limitar histórico grátis aos 3 concursos mais recentes | S | F3 | não iniciado | depende do RF-09 (Trevo Pro), que ainda não existe — sem estado "é assinante" pra gatear, igual à pendência já registrada em RF-01.8 |

## RF-07 · Perfil e notificações

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-07.1 | Editar nome/nascimento a qualquer momento, mesma validação | M | F2 | não iniciado | |
| RF-07.2 | Alterar crenças em tela dedicada, acessível pelo perfil | M | F2 | não iniciado | |
| RF-07.3 | Ligar/desligar lembrete antes do fechamento das apostas | M | F2 | não iniciado | |
| RF-07.4 | Horário livre do lembrete, atalhos, padrão 18h | M | F2 | não iniciado | |
| RF-07.5 | Alertar se horário escolhido ≥ 19h (após fechamento) | M | F2 | não iniciado | |
| RF-07.6 | Ligar/desligar notificação de resultado, independente | M | F2 | não iniciado | |
| RF-07.7 | Pedir permissão de notificação só ao ativar o primeiro aviso | M | F2 | não iniciado | |
| RF-07.8 | Exibir estado da assinatura, link para gerenciar na Play Store | M | F3 | não iniciado | |

## RF-08 · Compartilhamento e exportação

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-08.1 | Compartilhar palpite como texto (WhatsApp em destaque) | M | F1 | não iniciado | |
| RF-08.2 | Prévia da mensagem + copiar texto | M | F1 | não iniciado | |
| RF-08.3 | Exportar volante em PDF (com jogos do desdobramento) | S | F3 | não iniciado | |
| RF-08.4 | Exportar volante como imagem para a galeria | C | F3 | não iniciado | |

## RF-09 · Monetização

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-09.1 | 1 palpite/dia no grátis, reinício à meia-noite (fuso do aparelho) | M | F3 | não iniciado | |
| RF-09.2 | Anúncio recompensado de 15s libera palpite extra | S | F3 | não iniciado | |
| RF-09.3 | Nenhum outro formato de anúncio, nem para assinantes | M | F3 | não iniciado | |
| RF-09.4 | Planos anual/mensal, 7 dias grátis, via Play Billing | M | F3 | não iniciado | |
| RF-09.5 | Paywall informa cada etapa do teste antes da confirmação | M | F3 | não iniciado | |
| RF-09.6 | Liberar Pro imediatamente após compra; revogar ao fim | M | F3 | não iniciado | |
| RF-09.7 | Restaurar assinatura ao reinstalar/trocar de aparelho | M | F3 | não iniciado | |

## RF-10 · Transparência e jogo responsável

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-10.1 | Probabilidade real em toda tela de palpite/fechamento | M | F1 | não iniciado | |
| RF-10.2 | Nunca afirmar que método aumenta chance de acerto | M | F1 | não iniciado | |
| RF-10.3 | Deixar explícito que a aposta é feita pelo usuário na Caixa/lotérica | M | F1 | não iniciado | |
| RF-10.4 | Exibir gasto acumulado sem eufemismo, mesmo negativo | M | F2 | não iniciado | |
| RF-10.5 | Limite de gasto mensal configurável, com aviso | S | F4 | não iniciado | |

## RNF — Requisitos não funcionais

| ID | Requisito (resumo)                                                | Status |
|---|-------------------------------------------------------------------|---|
| RNF-01.1 | Abertura a frio ≤ 2s (aparelho de entrada, Android 10)            | não iniciado |
| RNF-01.2 | Geração de 15 dezenas < 100ms, sem rede                           | não iniciado |
| RNF-01.3 | Desdobramento de 20 dezenas fora da thread principal              | não iniciado |
| RNF-01.4 | Rolagem a 60fps nas listas                                        | não iniciado |
| RNF-01.5 | APK < 100 MB                                                      | não iniciado |
| RNF-02.1 | Todas as funções exceto busca de resultado operam offline         | não iniciado |
| RNF-02.2 | Nenhum palpite perdido por falha de rede/fechamento/reinício      | não iniciado |
| RNF-02.3 | Conferência pendente enfileirada, roda quando a rede volta        | não iniciado — decisão consciente de RF-05: retry manual (RF-05.8/05.9) cobre o requisito funcional explícito; fila automática via WorkManager fica pra outra fatia |
| RNF-02.4 | Recuo exponencial na busca de resultado, máx. 5 tentativas        | concluído — `RecuoExponencial.kt` (`:core:data`), testado |
| RNF-02.5 | Sessões sem falha > 99%, ANR < 0,47%                              | não iniciado |
| RNF-03.1 | Alvo de toque mínimo 48dp                                         | não iniciado |
| RNF-03.2 | Contraste 4,5:1 (texto corrido) / 3:1 (texto grande e UI)         | não iniciado |
| RNF-03.3 | Fonte do sistema até 200% sem corte                               | não iniciado |
| RNF-03.4 | TalkBack em todo elemento interativo; dezenas anunciam estado     | não iniciado |
| RNF-03.5 | Estado de marcação nunca só por cor                               | não iniciado |
| RNF-03.6 | Função principal alcançável em ≤ 3 toques da tela inicial         | não iniciado |
| RNF-03.7 | Interface em pt-BR; valores em real; datas dd/mm/aaaa             | não iniciado |
| RNF-04.1 | HTTPS obrigatório, texto claro desabilitado no manifesto          | concluído — `android:usesCleartextTraffic="false"` + base url HTTPS |
| RNF-04.2 | Nenhum dado financeiro coletado/armazenado/transmitido pelo app   | não iniciado |
| RNF-04.3 | Coleta limitada a nome e data de nascimento                       | não iniciado |
| RNF-04.4 | Conformidade LGPD (privacidade, consentimento, exclusão de dados) | não iniciado |
| RNF-04.5 | Formulário de segurança de dados da Play Store preenchido         | não iniciado |
| RNF-04.6 | Nenhuma permissão além de notificação e rede                      | não iniciado |
| RNF-05.1 | minSdk 26, targetSdk exigido pela Play Store na publicação        | não iniciado |
| RNF-05.2 | Layout correto 320–600dp de largura, retrato                      | não iniciado |
| RNF-05.3 | Tema escuro padrão, contraste sob alto contraste do sistema       | não iniciado |
| RNF-05.4 | Estado preservado em rotação/mudança de config/background         | não iniciado |
| RNF-06.1 | MVVM estrito; motor de geração sem dependência de Android         | não iniciado |
| RNF-06.2 | Cobertura de testes > 80% no motor, acertos e validações          | não iniciado |
| RNF-06.3 | Geração determinística sob semente fixa                           | não iniciado |
| RNF-06.4 | Testes de interface: cadastro, geração, edição, conferência       | não iniciado |
| RNF-06.5 | CI rodando build, lint e testes a cada envio                      | não iniciado |
| RNF-06.6 | Migrações Room versionadas, sem perda de dado                     | concluído para v1→v2 (`MIGRATION_1_2`, testada com `MigrationTestHelper`); reavaliar a cada nova migração |
| RNF-07.1 | Classificação etária 18+ coerente com o questionário              | não iniciado |
| RNF-07.2 | Loja declara que o app não recebe aposta nem paga prêmio          | não iniciado |
| RNF-07.3 | Toda transação pelo Play Billing                                  | não iniciado |
| RNF-07.4 | Assinatura: preço, periodicidade, teste declarados antes          | não iniciado |
| RNF-07.5 | Parecer jurídico antes da primeira publicação                     | não iniciado |
| RNF-08.1 | Log de falhas com stack trace, sem dado pessoal identificável     | não iniciado |
| RNF-08.2 | Eventos de produto instrumentados (palpite gerado, paywall, etc.) | não iniciado |
| RNF-08.3 | Falhas da API da Caixa registradas com código e latência          | não iniciado |
| RNF-08.4 | Coleta analítica sujeita a consentimento, desativável             | não iniciado |

## Fora de escopo (nunca implementar)

Receber valores de aposta · intermediar pagamento/custódia de bolão · registrar aposta na Caixa em nome do usuário · pagar prêmio ou operar premiação própria · prometer/sugerir/insinuar aumento de probabilidade · outras loterias além da Lotofácil na v1.