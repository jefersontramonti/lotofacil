# Trevo — Estado do projeto

Rastreia o status de cada requisito de `Docs/Trevo - Requisitos.dc.html`. Atualizado a cada `/nova-rf` (passo 7). Status possíveis: `não iniciado` · `em andamento` · `bloqueado` · `concluído`.

Legenda de prioridade (MoSCoW): **M** obrigatório na v1 · **S** importante mas adiável · **C** desejável.
Legenda de fase: **F1** MVP publicável · **F2** Retenção · **F3** Monetização · **F4** Escala.

## RF-01 · Cadastro e onboarding

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-01.1 | Tela inicial com proposta + aviso de aleatoriedade | M | F1 | concluído |  |
| RF-01.2 | Coletar nome e data de nascimento em campos separados | M | F1 | concluído | bug relatado pelo usuário: o app sempre abria em `Rotas.ABERTURA`, sem checar se já havia perfil salvo — nome/nascimento eram gravados (`CrencasViewModel.aoGerarPalpite` → `PreferenciasRepository.salvarPerfil`) mas nunca lidos de volta no início, então todo relançamento repetia o onboarding inteiro. Corrigido com `InicioViewModel` (expõe `perfilJaExiste: StateFlow<Boolean?>`); `TrevoNavHost` espera esse valor antes de montar o `NavHost` e escolhe `Rotas.HOME` quando já existe perfil. Verificado no emulador (onboarding uma vez → force-stop → reabrir → cai direto na Home) |
| RF-01.3 | Validar data dd/mm/aaaa (mês, dia, bissexto, ano 1900–hoje) | M | F1 | concluído |  |
| RF-01.4 | Bloquear cadastro de menores de 18 anos | M | F1 | concluído |  |
| RF-01.5 | Calcular signo a partir da data válida; marcador neutro se inválida | M | F1 | concluído |  |
| RF-01.6 | Impedir avanço do passo com erro de validação | M | F1 | concluído | |
| RF-01.7 | Apresentar as 12 crenças, seleção múltipla | M | F1 | concluído | |
| RF-01.8 | Limitar seleção a 3 crenças no grátis, cadeado leva ao paywall | M | F3 | concluído | `onCrencaBloqueadaClick` (onboarding e edição de crenças no perfil) navega pra `Rotas.PAYWALL`; `CrencasUiState.isPro` agora vem de `AssinaturaRepository.observarIsPro()`, não mais hardcoded |
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
| RF-03.1 | Concurso corrente, horário de fechamento (19h) e sorteio (20h) | M | F1 | concluído | horário corrigido pra 20h/21h (`TelaHome`) — o resumo "(19h)/(20h)" no próprio texto do requisito está errado contra `Docs/tabelavalores.md` (fonte oficial: apostas até 20h, sorteio às 21h, seg-sáb); avise pra corrigir o requisito. Número do concurso: `HomeViewModel` deriva `numeroDoConcursoCorrente = últimoResultado.numero + 1` a partir de `ResultadoRepository.observarUltimoResultadoSalvo()` — a numeração da Caixa nunca pula (mesmo sem sorteio aos domingos), então "último + 1" é aritmética sobre um número real, não um dado de sorteio inventado (CLAUDE.md §8, que proíbe inventar resultado/dezenas/prêmio, não um incremento sequencial). Fica `null` até o primeiro resultado real ser buscado (RF-05) ou quando o último salvo foi entrada manual sem número (RF-05.10) — decisão confirmada com o usuário antes de implementar. Verificado no emulador com chamada real à API da Caixa: "Concurso 3767 · apostas até 20h · sorteio às 21h". **Card do próximo concurso** (`CartaoProximoConcurso`, `TelaHome`): quando existe, substitui o texto simples e mostra número, data, prêmio estimado e valor acumulado — dados reais de `numeroConcursoProximo`/`dataProximoConcurso`/`valorEstimadoProximoConcurso`/`valorAcumuladoProximoConcurso`, campos confirmados via chamada real à API (não documentados em nenhum doc do projeto) e persistidos em `Resultado.proximoConcurso` (`ProximoConcurso`, `:core:engine`; migração Room `MIGRATION_3_4`, versão 3→4). `HomeViewModel` dispara uma busca best-effort em segundo plano ao abrir a Home pra manter esse cache atualizado sem bloquear a tela se a rede falhar (mesmo padrão de `ResultadoSorteioWorker`) |
| RF-03.2 | Índice de sorte do dia, fase da lua, signo | S | F1 | concluído | índice de sorte é fórmula decorativa nova (sem base em nenhum doc — protótipo só tinha valor mockado); lua/signo usam cálculo já existente do RF-02 |
| RF-03.3 | Seletor dos 25 grupos do jogo do bicho quando sonho ativo | M | F1 | concluído | prévia de 4 + expansão inline pros 25; gated por `Crenca.SONHO` no perfil salvo |
| RF-03.4 | Listar palpites do dia com dezenas, horário, força, crenças | M | F1 | concluído | crenças usadas por palpite ainda não aparecem no card, só dezenas/horário/força |
| RF-03.5 | Indicar dezenas que mudaram vs. palpite anterior | S | F1 | concluído | |
| RF-03.6 | Total de jogos do dia e custo na lotérica | M | F1 | concluído | |
| RF-03.7 | Estado vazio sem palpites | M | F1 | concluído | |
| RF-03.8 | Excluir palpite com confirmação (lista e detalhe) | M | F1 | concluído | confirmação em `TelaHome` (lista) e `TelaDetalhe` (detalhe, desde RF-04) — nota anterior estava desatualizada, RF-04 já concluiu antes desta revisão |
| RF-03.9 | Exibir palpites restantes no dia / ilimitado no Pro | M | F3 | concluído | `HomeUiState.palpitesGratisRestantesHoje`/`isPro`, texto acima do CTA (`home_restantes_gratis`/`home_restantes_pro`), wireframe 1d/1e |
| RF-03.10 | Tocar grupo abre card com nome, número, leitura, dezenas | M | F1 | concluído | `DialogoCartaoDoSonho`, wireframe 1t |
| RF-03.11 | Leitura escrita pros 25 grupos, tradição popular | M | F1 | concluído | conteúdo portado do protótipo de referência (`GRUPOS_DO_BICHO`) |
| RF-03.12 | Confirmar grupo como sonho do dia, indicar já escolhido | M | F1 | concluído | confirmação e persistência (DataStore) prontas; agora alimenta geração real — `HomeViewModel.aoGerarClick`/`RitualViewModel` leem `observarGrupoDoSonhoDeHoje` pro `DadosDeContribuicao` (RF-11) |
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
| RF-04.10 | Listar combinações do desdobramento (limitado em tela) | S | F3 | concluído | `combinacoesDe15` (sequence preguiçosa) + `TelaDesdobramentos`, limite de 24. Agora alcançável de verdade: RF-09 existe, 16/18/20 destravam com `isPro` real no ritual e no seletor de `TelaDetalhe` |

## RF-05 · Resultados e conferência

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-05.1 | Buscar resultado oficial na API da Caixa | M | F1 | concluído | `ResultadoApi`/`ResultadoRepositoryImpl` (`:core:data`), API real da Caixa (`servicebus2.caixa.gov.br`), verificado com chamada de rede real no emulador |
| RF-05.2 | Armazenar resultados localmente (consulta offline) | M | F1 | concluído | tabela `resultados` (Room, migração `MIGRATION_1_2` versão 1→2, testada com `MigrationTestHelper`) |
| RF-05.3 | Conferir automaticamente todos os palpites do concurso | M | F2 | concluído | roda ao entrar na `TelaConferencia` (`ConferenciaViewModel.aoEntrar`), não em background/WorkManager — ver pendência de RNF-02.3 |
| RF-05.4 | Exibir acertos, faixa premiada e valor do prêmio por palpite | M | F1 | concluído | `conferir()` (`:core:engine`) + `TelaConferencia`. Card do concurso ganhou também data do sorteio, aviso de "Concurso acumulado" e a tabela oficial completa de premiação (11 a 15 acertos, `SecaoPremiacao`/`resultado.faixasDePremio`) — dado real já vinha da API mas não era exibido; ausente pra resultado manual (RF-05.10, sem `faixasDePremio`) |
| RF-05.5 | Destacar no volante: acerto, marcada não sorteada, sorteada não marcada | M | F1 | concluído | `estadosDasDezenas()` (`:core:engine`, as 25 dezenas); a tela de Conferência em si usa o resumo compacto por palpite do wireframe 1j (cheia=acerto/tracejada=miss), igual ao protótipo — a grade 5×5 completa com os 3 estados fica reservada pra quando `TelaDetalhe` ganhar essa integração. Achado numa verificação contra 1j/1k: a legenda ("Bola cheia = acerto, tracejada = marcada que não saiu") prometia um contorno tracejado que o código nunca desenhava — as duas bolas só se distinguiam pela cor de fundo, o que também violava RNF-03.5 (marcação nunca só por cor). Corrigido com `Modifier.bordaCircularTracejada` (Canvas + `PathEffect.dashPathEffect`, `TelaConferencia.kt`) |
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
| RF-06.6 | Limitar histórico grátis aos 3 concursos mais recentes | S | F3 | concluído | `HistoricoViewModel` corta `concursos`/estatísticas em 3 quando `!isPro` (as estatísticas refletem só o que o grátis vê, nunca um total agregando concurso escondido); `maisConcursosSoNoPro` mostra o CTA de assinar quando há mais concursos além do limite |

## RF-07 · Perfil e notificações

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-07.1 | Editar nome/nascimento a qualquer momento, mesma validação | M | F2 | concluído | `TelaPerfil`/`PerfilViewModel` reaproveitam `ValidadorDataNascimento`/`VerificadorDeIdade`/`formatarDataNascimento`/`signoDe` do RF-01 (mesma fonte de validação, não um parser paralelo); grava a cada alteração — nome sempre, nascimento/signo só quando a data é válida e maior de idade, nunca sobrescrevendo o valor salvo com um estado inválido/incompleto |
| RF-07.2 | Alterar crenças em tela dedicada, acessível pelo perfil | M | F2 | concluído | reaproveita `TelaCrencas` do onboarding (mesmos cartões, mesmo limite de 3 no grátis) via `EditarCrencasViewModel` novo, rota `perfil_crencas`; `textoContinuar` virou parâmetro pra trocar "Entrar no app" por "Salvar" sem duplicar a tela |
| RF-07.3 | Ligar/desligar lembrete antes do fechamento das apostas | M | F2 | concluído | `LembreteFechamentoWorker` (WorkManager + Hilt) dispara e se reagenda pro dia seguinte sozinho; toggle liga/desliga o agendamento na hora via `NotificacoesScheduler`, sem esperar o worker rodar |
| RF-07.4 | Horário livre do lembrete, atalhos, padrão 18h | M | F2 | concluído | chip do horário atual (abre `TimePicker` livre) + 3 atalhos fixos (17h/18h/18h30); padrão 18h como no wireframe |
| RF-07.5 | Alertar se horário escolhido ≥ fechamento das apostas | M | F2 | concluído | wireframe 1m e o texto do requisito dizem "19h", mas RF-03.1 já tinha corrigido esse número pra 20h contra `Docs/tabelavalores.md` (fonte oficial: apostas até 20h, sorteio às 21h) — RF-07 segue a correção já aplicada em `TelaHome`, não o número desatualizado do wireframe; mesma razão vale pro aviso de resultado (RF-07.6): o wireframe diz "20h", mas o sorteio só sai às 21h, então o aviso foi pra 21h30 |
| RF-07.6 | Ligar/desligar notificação de resultado, independente | M | F2 | concluído | `ResultadoSorteioWorker`, horário fixo 21h30 (não configurável, ver nota de RF-07.5); só notifica se `buscarUltimoResultado()` devolver o resultado de hoje — nunca reavisa sobre um concurso já visto (CLAUDE.md §8) |
| RF-07.7 | Pedir permissão de notificação só ao ativar o primeiro aviso | M | F2 | concluído | evento one-shot do ViewModel (`PerfilEvento.PedirPermissaoDeNotificacao`) disparado só dentro do toggle-liga, nunca num `LaunchedEffect(Unit)` de abertura de tela; `TrevoNavHost` decide se precisa pedir (`ContextCompat.checkSelfPermission`, Android 13+) |
| RF-07.8 | Exibir estado da assinatura, link para gerenciar na Play Store | M | F3 | concluído | card mostra `Gratuito`/`Assinante` real (`AssinaturaRepository.observarAssinatura()`); toque abre o paywall (grátis) ou `https://play.google.com/store/account/subscriptions` (Pro) — nunca dentro do app (CLAUDE.md §1). Sem data de renovação: o client do Billing não expõe isso (só Play Developer API/servidor), então não inventamos uma |

## RF-11 · Modos de geração e ritual dos amuletos

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-11.1 | Três modos na home (Místico/Cientista/Destino) com descrição curta | M | F1 | concluído | `SecaoModoDeGeracao` (`TelaHome`), seleção transiente em `HomeViewModel` (`modoSelecionado`, não persistida — reabrir a Home volta pro Místico) |
| RF-11.2 | Filtrar crenças efetivamente aplicadas por modo | M | F1 | concluído | `crencasAtivasNoModo` (`:core:engine`) já existia (sem UI até agora); `HomeViewModel.aoGerarClick` e `RitualViewModel` agora chamam de verdade |
| RF-11.3 | Destino troca o botão de gerar pelo que inicia o ritual | M | F1 | concluído | `TrevoNavHost` decide no `onCtaPrincipalClick` da Home: Destino navega pra `Rotas.RITUAL`, os outros dois chamam `aoGerarClick` direto |
| RF-11.4 | Amuletos em sequência (8 amuletos, 4/3/3/2/3/3/3/3 opções), com conceito/pergunta/opções | M | F1 | concluído | `Amuleto`/`OpcaoDeAmuleto` (`:core:engine`), ordem fixa `ORDEM_DO_RITUAL`: trevo → ferradura → anéis → moedas → bola de cristal → dados → elefante → estrela (24 opções ao todo); textos em `AmuletoTextos.kt`. Conjunto corrigido depois de divergir do protótipo (`Docs/Trevo - Lotofácil.dc.html`, array `AMULETOS`) — a primeira versão desta fatia só tinha 3 amuletos (faltava a moeda) — e depois expandido pra 8 a pedido explícito do usuário, com o protótipo e o Requisitos atualizados juntos pra não ficarem desalinhados do código |
| RF-11.5 | Nunca revelar a dezena antes da escolha; opção nunca insinua qual dezena esconde | M | F1 | concluído | a dezena só existe a partir de `RitualViewModel.aoEscolherOpcao`; rótulos das opções são só posição/identidade (ex.: "do meio"), nunca número |
| RF-11.6 | Revelação com giro/halo, mostrando amuleto/conceito/frase | S | F1 | concluído | `CirculoDeRevelacao` (`TelaRitual`), halo animado; respeita movimento reduzido do sistema igual a `TelaGerando` |
| RF-11.7 | Sortear a dezena pelo motor de pesos, excluindo já reveladas | M | F1 | concluído | `PalpiteGenerator.sortearDezenaDoRitual` — mesma fórmula de peso de `gerar()` (refatorada pra dois helpers privados compartilhados, sem duplicar a fórmula), testado que nunca repete entre 3 sorteios sucessivos |
| RF-11.8 | Trilha de progresso e dezenas já reveladas visíveis durante o ritual | S | F1 | concluído | 8 chips no cabeçalho, um por amuleto (preenchido = feito) + lista "já revelado" na tela de escolha |
| RF-11.9 | Resumo final: cada amuleto com dezena/frase, contagem do resto, refazer ou montar | M | F1 | concluído | `TelaResumoDoRitual`; "↻" chama `aoRefazerRitualClick` (reseta o estado local, sem tocar em nada persistido). O resumo também tem o seletor de fechamento (RF-02.8: 15/16/18/20) — `RitualViewModel.aoEscolherTamanho`/`RitualUiState.Resumo.tamanho`, 16/18/20 destravam com `isPro` real (RF-09, `AssinaturaRepository.observarIsPro()`) em vez do hardcoded `false` de antes. Bug relatado pelo usuário: nem `TelaEscolhaDoAmuleto` nem `TelaResumoDoRitual` tinham `verticalScroll` (única tela do app sem isso — Home/Detalhe/Conferência/Perfil já usavam), então dava pra travar sem conseguir ver o resto do conteúdo, principalmente depois do seletor de fechamento deixar o resumo mais alto. Corrigido em ambas |
| RF-11.10 | Forçar dezenas reveladas no volante final; registrar como fonte própria na origem | M | F1 | concluído | as 8 dezenas entram via `dezenasFixas` (mesmo mecanismo de RF-02.2/RF-04.7 — reaproveitado, não duplicado; as outras 7 do palpite de 15 vêm das crenças/estatística); `Palpite.ritual: List<RevelacaoDoAmuleto>` é metadado à parte pra exibição, persistido em `PalpiteEntity.ritual` (migração `MIGRATION_2_3`); aparece em "De onde vieram as dezenas" (`TelaDetalhe`) ao lado das crenças |
| RF-11.11 | Limpar o ritual após a montagem, sem reaproveitar dezenas do anterior | M | F1 | concluído | `RitualViewModel` não persiste nada — sair da rota (fechar ou montar) destrói o ViewModel e com ele qualquer revelação em andamento; é a mesma causa-raiz do bug do protótipo que CLAUDE.md §4 documenta, resolvida por nunca reter o estado em vez de limpar explicitamente algo compartilhado |
| RF-11.12 | Tela do ritual explicita que a escolha é do usuário mas a dezena é sorteada, sem alterar probabilidade | M | F1 | concluído | `ritual_disclaimer_escolha` fixo na tela de escolha de cada amuleto |
| RF-11.13 | Identificar o modo nas etiquetas do palpite na home | S | F1 | concluído | tag do modo no `CartaoPalpite`; `null` pros palpites de antes do RF-11 ou gerados pelo onboarding (nunca inventa um modo retroativo) |
| RF-11.14 | Comparar desempenho histórico entre os modos (média de acertos) | C | F4 | não iniciado | fase 4 (Escala) e prioridade C — adiado deliberadamente; exigiria cruzar `Palpite.modo` com conferência (RF-05/RF-06), série temporal ainda não modelada |

## RF-08 · Compartilhamento e exportação

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-08.1 | Compartilhar palpite como texto (WhatsApp em destaque) | M | F1 | concluído | `TelaDetalhe` (ícone 📤 no cabeçalho) + `FolhaDeCompartilhamento`; tenta abrir direto no WhatsApp (`setPackage("com.whatsapp")`) e cai no `Intent.createChooser` do sistema se o app não estiver instalado — cobre as duas metades do requisito. Número do concurso na mensagem fica `null` (omitido) quando o dia do palpite ainda não tem `Resultado` casado — mesma regra de nunca inventar dado de sorteio de RF-03.1/RF-06.1 (CLAUDE.md §8) |
| RF-08.2 | Prévia da mensagem + copiar texto | M | F1 | concluído | mesma folha do RF-08.1: caixa de prévia com a mensagem montada, botão "Copiar o texto" (`ClipboardManager` do sistema) e confirmação "Pronto para enviar" |
| RF-08.3 | Exportar volante em PDF (com jogos do desdobramento) | S | F3 | concluído | `VolantePdfExporter.gerarPdfDoVolante` (`android.graphics.pdf.PdfDocument`, sem dependência nova) roda em `Dispatchers.Default` (RNF-01.3) e inclui todos os jogos do fechamento quando >15 dezenas — um fechamento de 20 vira até ~345 páginas (15.504 jogos), não só uma prévia. `FileProvider` novo (manifest + `res/xml/file_paths.xml`) expõe o PDF do cache do app via `content://`; abre no seletor do sistema (`ACTION_SEND`), mesmo espírito do RF-08.1. Ícone "⤓" no cabeçalho de `TelaDetalhe`, mesmo padrão bloqueada/desbloqueada de `TelaCrencas.onCrencaClick`/`onCrencaBloqueadaClick` — sem Pro mostra "⤓🔒" e leva ao paywall. Testado ponta a ponta no emulador (ícone bloqueado → paywall, sem crash) e com teste instrumentado real gerando o PDF (`VolantePdfExporterTest`, 15 e 16 dezenas) |
| RF-08.4 | Exportar volante como imagem para a galeria | C | F3 | não iniciado | mesma dependência de RF-08.3 (Pro), mas é MediaStore/Bitmap, escopo separado |

## RF-09 · Monetização

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-09.1 | 1 palpite/dia no grátis, reinício à meia-noite (fuso do aparelho) | M | F3 | concluído | `PreferenciasRepository.observarPalpitesGratisRestantesHoje`/`registrarPalpiteGratisUsado` — mesmo padrão de data-salva-vs-hoje de `confirmarGrupoDoSonho` (RF-03.12), reset é automático por virada de dia, não um job separado. Gateia `HomeViewModel.aoGerarClick` e `RitualViewModel.aoMontarPalpiteClick` |
| RF-09.2 | Anúncio recompensado de 15s libera palpite extra | S | F3 | concluído | `AnuncioRecompensadoManager` (`:app`, sem Hilt — só usa Google Mobile Ads SDK) + `registrarAnuncioAssistido`; usa o ad unit de TESTE do Google (`ca-app-pub-3940256099942544/5224354917`) — funciona sem conta AdMob, mas precisa trocar pelo real antes de publicar. Limite de 2 anúncios/dia (`LIMITE_ANUNCIOS_POR_DIA`, `PreferenciasRepositoryImpl`), reset por virada de dia igual ao limite grátis; `TelaHome` mostra "N anúncios disponíveis hoje" embaixo do CTA enquanto sobrar cota e esconde o botão (só resta assinar) ao esgotar. Verificado no emulador com o SDK real do AdMob: dois anúncios assistidos até o fim creditam 1 palpite cada, terceiro pedido não mostra mais o botão. **Achado de auditoria de segurança (security-auditor, 2026-08-22):** o crédito da recompensa era decidido 100% no cliente, sem nenhuma verificação fora do aparelho — um APK repackeado podia chamar `HomeViewModel.aoAnuncioRecompensado()` direto e creditar sem nunca mostrar o anúncio. Mitigação aplicada (sem backend, decisão do usuário): `AnuncioRecompensadoManager` agora emite um token de uso único quando o SDK realmente carrega um anúncio (`aoCarregar`), e `HomeViewModel.aoAnuncioRecompensado(token)` só credita se o token bater com o que `aoAnuncioCarregado` registrou — fecha o caminho mais barato (botão reconectado direto num APK adulterado), mas **não é Server-Side Verification (SSV) e não resiste a instrumentação em tempo de execução (Frida/Xposed) num aparelho rooteado**. Dívida registrada: implementar SSV real do AdMob exige backend próprio (hoje o app não tem nenhum, CLAUDE.md §4 — única dependência de rede é a API de resultado da Caixa); decisão de arquitetura pendente para quando/se o Trevo ganhar backend |
| RF-09.3 | Nenhum outro formato de anúncio, nem para assinantes | M | F3 | concluído | cumprido por omissão — só existe o `AnuncioRecompensadoManager` recompensado, nenhum banner/intersticial em lugar nenhum do app |
| RF-09.4 | Planos anual/mensal, 7 dias grátis, via Play Billing | M | F3 | concluído | `AssinaturaRepository`/`AssinaturaRepositoryImpl` (`:core:data`), `billing-ktx` real (`BillingClient`, não mock). IDs de produto usados no código: `trevo_pro_mensal`/`trevo_pro_anual` — **pendente de verdade**: precisam ser criados no Play Console (produto + base plan + oferta de teste de 7 dias) pelo usuário; sem isso `produtosDisponiveis()` devolve lista vazia e o paywall mostra "indisponível", nunca preço inventado. Bug real achado só em device: `PendingPurchasesParams.newBuilder().build()` sem `.enableOneTimeProducts()` lança `IllegalArgumentException` em runtime (Billing 7.1.1 exige a declaração mesmo o Trevo só vendendo assinatura) — derrubava toda tela que injeta `AssinaturaRepository` (Crenças, Home, Ritual, Detalhe, Histórico, Perfil) assim que instanciada; não pegou em `test`/`lint`/`ktlintCheck` porque só estoura ao construir o `BillingClient` real. Corrigido; app testado ponta a ponta no emulador (onboarding → Home → gerar/limite/anúncio → paywall → Detalhe → Perfil → Histórico) sem crash |
| RF-09.5 | Paywall informa cada etapa do teste antes da confirmação | M | F3 | concluído | `TelaPaywall` (wireframe 1n): caixa "Como funciona o teste" com a linha do tempo (hoje/dia 5/dia 7) antes do CTA "Começar 7 dias grátis" |
| RF-09.6 | Liberar Pro imediatamente após compra; revogar ao fim | M | F3 | concluído | `EstadoDaAssinatura` deriva sempre de `BillingClient.queryPurchasesAsync` (nunca uma flag local solta) — se o Billing para de devolver a compra, o estado volta pra `Gratuito` sozinho. `PurchasesUpdatedListener` libera na hora após `launchBillingFlow` |
| RF-09.7 | Restaurar assinatura ao reinstalar/trocar de aparelho | M | F3 | concluído | `AssinaturaRepository.restaurarCompras()` reconsulta `queryPurchasesAsync`; já roda sozinho na conexão inicial do `BillingClient` (`init` do repositório), então reinstalar já restaura sem ação do usuário |

## RF-10 · Transparência e jogo responsável

| ID | Requisito (resumo) | Pri | Fase | Status | Branch/PR |
|---|---|---|---|---|---|
| RF-10.1 | Probabilidade real em toda tela de palpite/fechamento | M | F1 | concluído | cumprido incidentalmente por outras fatias, nunca teve slice própria: `detalhe_chance_valor`/`probabilidadeDe15Acertos` (RF-04.4) no Detalhe; nota anterior desta tabela estava desatualizada |
| RF-10.2 | Nunca afirmar que método aumenta chance de acerto | M | F1 | concluído | disclaimers em `home_disclaimer_aposta`, `ritual_disclaimer_escolha` (RF-11.12), `home_sonho_card_disclaimer` (RF-03.13) — nenhum texto do app afirma aumento de chance; nota anterior estava desatualizada |
| RF-10.3 | Deixar explícito que a aposta é feita pelo usuário na Caixa/lotérica | M | F1 | concluído | `home_disclaimer_aposta` (Home); nota anterior estava desatualizada |
| RF-10.4 | Exibir gasto acumulado sem eufemismo, mesmo negativo | M | F2 | concluído | `HistoricoUiState.saldo`/RF-06.2 exibe negativo sem eufemismo, verificado no emulador; nota anterior estava desatualizada |
| RF-10.5 | Limite de gasto mensal configurável, com aviso | S | F4 | não iniciado | único item real do bloco ainda pendente — sem tela/preferência de limite de gasto |

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
| RNF-06.6 | Migrações Room versionadas, sem perda de dado                     | concluído para v1→v2 e v2→v3 (`MIGRATION_1_2`/`MIGRATION_2_3`, testadas com `MigrationTestHelper`); reavaliar a cada nova migração |
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