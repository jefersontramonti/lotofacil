package com.trevo.app.navegacao

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trevo.app.R
import com.trevo.app.assinatura.PaywallViewModel
import com.trevo.app.assinatura.TelaPaywall
import com.trevo.app.conferencia.ConferenciaViewModel
import com.trevo.app.conferencia.TelaConferencia
import com.trevo.app.detalhe.DesdobramentosViewModel
import com.trevo.app.detalhe.DetalheViewModel
import com.trevo.app.detalhe.TelaDesdobramentos
import com.trevo.app.detalhe.TelaDetalhe
import com.trevo.app.detalhe.gerarPdfDoVolante
import com.trevo.app.geracao.GeracaoViewModel
import com.trevo.app.geracao.TelaGerando
import com.trevo.app.geracao.movimentoReduzidoAtivado
import com.trevo.app.historico.HistoricoViewModel
import com.trevo.app.historico.TelaHistorico
import com.trevo.app.home.AnuncioRecompensadoManager
import com.trevo.app.home.HomeViewModel
import com.trevo.app.home.TelaHome
import com.trevo.app.onboarding.CrencasViewModel
import com.trevo.app.onboarding.IdentidadeViewModel
import com.trevo.app.onboarding.TelaAbertura
import com.trevo.app.onboarding.TelaCrencas
import com.trevo.app.onboarding.TelaIdentidade
import com.trevo.app.perfil.EditarCrencasViewModel
import com.trevo.app.perfil.PerfilEvento
import com.trevo.app.perfil.PerfilViewModel
import com.trevo.app.perfil.TelaPerfil
import com.trevo.app.ritual.RitualEvento
import com.trevo.app.ritual.RitualViewModel
import com.trevo.app.ritual.TelaRitual
import com.trevo.core.engine.crenca.ModoDeGeracao
import kotlinx.coroutines.launch

@Composable
fun TrevoNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (rotaAtual in ROTAS_COM_BARRA_DE_NAVEGACAO) {
                BarraDeNavegacaoDoApp(rotaAtual = rotaAtual, navController = navController)
            }
        },
    ) { paddingDoConteudo ->
        NavHost(
            navController = navController,
            startDestination = Rotas.ABERTURA,
            modifier = Modifier.padding(paddingDoConteudo),
        ) {
            composable(Rotas.ABERTURA) {
                TelaAbertura(onComecarClick = { navController.navigate(Rotas.IDENTIDADE) })
            }
            composable(Rotas.IDENTIDADE) {
                val viewModel: IdentidadeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                TelaIdentidade(
                    uiState = uiState,
                    onNomeChange = viewModel::aoAlterarNome,
                    onNascimentoChange = viewModel::aoAlterarNascimento,
                    onVoltarClick = { navController.popBackStack() },
                    onContinuarClick = { navController.navigate(Rotas.CRENCAS) },
                )
            }
            composable(Rotas.CRENCAS) { entradaAtual ->
                val viewModel: CrencasViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                // A entrada de IDENTIDADE continua na pilha (só POP_BACK a
                // desfaz) — reaproveita o mesmo ViewModel em vez de duplicar
                // nome/nascimento/signo como argumento de rota. `remember` usa
                // a entrada da própria rota CRENCAS como chave, não o
                // navController (regra UnrememberedGetBackStackEntry do lint
                // do Navigation Compose).
                val identidadeEntry =
                    remember(entradaAtual) { navController.getBackStackEntry(Rotas.IDENTIDADE) }
                val identidadeViewModel: IdentidadeViewModel = hiltViewModel(identidadeEntry)
                val identidadeUiState by identidadeViewModel.uiState.collectAsState()

                TelaCrencas(
                    uiState = uiState,
                    onCrencaClick = viewModel::aoTocarCrenca,
                    onCrencaBloqueadaClick = { navController.navigate(Rotas.PAYWALL) },
                    onVoltarClick = { navController.popBackStack() },
                    onContinuarClick = {
                        // O palpite é gerado e salvo (assíncrono — ver
                        // CrencasViewModel.aoGerarPalpite) antes da navegação; a
                        // Home observa o repositório por Flow, então o card
                        // aparece assim que a escrita completar. Entre Crenças e
                        // Home entra a tela de ritual (RF-02.9) — o popUpTo só
                        // acontece de lá pra Home, então "voltar" durante o
                        // ritual ainda cai em Crenças.
                        viewModel.aoGerarPalpite(
                            nome = identidadeUiState.nome,
                            nascimentoTexto = identidadeUiState.nascimento,
                            signo = identidadeUiState.signo,
                        )
                        navController.navigate(Rotas.GERANDO)
                    },
                )
            }
            composable(Rotas.GERANDO) {
                val viewModel: GeracaoViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.iniciar(movimentoReduzido = movimentoReduzidoAtivado(context))
                }
                LaunchedEffect(uiState.concluido) {
                    if (uiState.concluido) {
                        navController.navigate(Rotas.HOME) {
                            popUpTo(Rotas.ABERTURA) { inclusive = true }
                        }
                    }
                }

                TelaGerando(uiState = uiState, movimentoReduzido = movimentoReduzidoAtivado(context))
            }
            composable(Rotas.HOME) {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current
                // RF-09.2 — sem Hilt de propósito (ver AnuncioRecompensadoManager);
                // `remember` na própria rota mantém o anúncio pré-carregado vivo
                // entre recomposições da Home, sem sobreviver à navegação pra fora.
                val gerenciadorDeAnuncio = remember { AnuncioRecompensadoManager() }
                LaunchedEffect(Unit) { gerenciadorDeAnuncio.carregar(context) }

                TelaHome(
                    uiState = uiState,
                    onExcluirClick = viewModel::aoPedirExclusao,
                    onConfirmarExclusaoClick = viewModel::aoConfirmarExclusao,
                    onCancelarExclusaoClick = viewModel::aoCancelarExclusao,
                    onPalpiteClick = { id -> navController.navigate(Rotas.detalhe(id)) },
                    onAlternarListaDeGruposClick = viewModel::aoAlternarListaDeGrupos,
                    onGrupoClick = viewModel::aoAbrirGrupo,
                    onFecharDialogoSonhoClick = viewModel::aoFecharDialogDoSonho,
                    onConfirmarSonhoClick = viewModel::aoConfirmarSonho,
                    onSelecionarModoClick = viewModel::aoSelecionarModo,
                    onCtaPrincipalClick = {
                        // RF-11.3 — só o modo Destino abre o ritual; Místico e
                        // Cientista geram direto por HomeViewModel.aoGerarClick.
                        // RF-09.1 — este CTA só é renderizado quando ainda há
                        // palpite grátis (ou é Pro), ver TelaHome.SecaoModoDeGeracao.
                        if (uiState.modoSelecionado == ModoDeGeracao.DESTINO) {
                            navController.navigate(Rotas.RITUAL)
                        } else {
                            viewModel.aoGerarClick()
                        }
                    },
                    onAssistirAnuncioClick = {
                        context.comoActivity()?.let { activity ->
                            gerenciadorDeAnuncio.exibir(
                                activity = activity,
                                aoGanharRecompensa = viewModel::aoAnuncioRecompensado,
                                aoFechar = { gerenciadorDeAnuncio.carregar(context) },
                            )
                        }
                    },
                    onAssinarClick = { navController.navigate(Rotas.PAYWALL) },
                )
            }
            composable(Rotas.RITUAL) {
                val viewModel: RitualViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(viewModel) {
                    viewModel.eventos.collect { evento ->
                        when (evento) {
                            RitualEvento.PalpiteMontado -> navController.popBackStack()
                        }
                    }
                }

                TelaRitual(
                    uiState = uiState,
                    onFecharClick = { navController.popBackStack() },
                    onEscolherOpcao = viewModel::aoEscolherOpcao,
                    onRevelacaoTerminou = viewModel::aoRevelacaoTerminou,
                    onRefazerClick = viewModel::aoRefazerRitualClick,
                    onEscolherTamanhoClick = viewModel::aoEscolherTamanho,
                    onTamanhoBloqueadoClick = { navController.navigate(Rotas.PAYWALL) },
                    onMontarPalpiteClick = viewModel::aoMontarPalpiteClick,
                    movimentoReduzido = movimentoReduzidoAtivado(context),
                )
            }
            composable(Rotas.CONFERENCIA) {
                val viewModel: ConferenciaViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(Unit) { viewModel.aoEntrar() }

                TelaConferencia(
                    uiState = uiState,
                    onVoltarClick = { navController.popBackStack() },
                    onTentarNovamenteClick = viewModel::aoTentarNovamente,
                    onInformarResultadoManualmente = viewModel::aoInformarResultadoManualmente,
                )
            }
            composable(Rotas.HISTORICO) {
                val viewModel: HistoricoViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                TelaHistorico(
                    uiState = uiState,
                    onVerMaisClick = viewModel::aoVerMaisClick,
                    onAssinarClick = { navController.navigate(Rotas.PAYWALL) },
                )
            }
            composable(Rotas.PERFIL) {
                val viewModel: PerfilViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                // RF-07.7: a permissão de notificação (Android 13+) só é
                // pedida aqui, disparada pelo evento do primeiro toggle
                // ligado — nunca no LaunchedEffect(Unit) de abertura da tela.
                val lancadorDePermissao =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
                LaunchedEffect(viewModel) {
                    viewModel.eventos.collect { evento ->
                        when (evento) {
                            PerfilEvento.PedirPermissaoDeNotificacao -> {
                                val jaConcedida =
                                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS,
                                        ) ==
                                        PackageManager.PERMISSION_GRANTED
                                if (!jaConcedida) lancadorDePermissao.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                }

                TelaPerfil(
                    uiState = uiState,
                    onNomeChange = viewModel::aoAlterarNome,
                    onNascimentoChange = viewModel::aoAlterarNascimento,
                    onCrencasClick = { navController.navigate(Rotas.PERFIL_CRENCAS) },
                    onAlternarLembreteFechamento = viewModel::aoAlternarLembreteFechamento,
                    onEscolherHorarioLembrete = viewModel::aoEscolherHorarioLembrete,
                    onAlternarNotificacaoResultado = viewModel::aoAlternarNotificacaoResultado,
                    onAssinaturaClick = {
                        // RF-07.8/RF-09.7 — gerenciar (cancelar/trocar plano) é
                        // sempre na Play Store, nunca dentro do app; grátis abre
                        // o paywall igual aos outros pontos de entrada.
                        val produtoId = uiState.productIdDaAssinatura
                        if (uiState.isPro && produtoId != null) {
                            abrirGerenciamentoDaAssinatura(context, produtoId)
                        } else {
                            navController.navigate(Rotas.PAYWALL)
                        }
                    },
                )
            }
            composable(Rotas.PERFIL_CRENCAS) {
                val viewModel: EditarCrencasViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                TelaCrencas(
                    uiState = uiState,
                    onCrencaClick = viewModel::aoTocarCrenca,
                    onCrencaBloqueadaClick = { navController.navigate(Rotas.PAYWALL) },
                    onVoltarClick = { navController.popBackStack() },
                    onContinuarClick = {
                        viewModel.aoSalvarClick()
                        navController.popBackStack()
                    },
                    textoContinuar = stringResource(id = R.string.crencas_cta_salvar),
                )
            }
            composable(
                Rotas.DETALHE,
                arguments = listOf(navArgument("palpiteId") { type = NavType.LongType }),
            ) { entrada ->
                val viewModel: DetalheViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val palpiteId = entrada.arguments?.getLong("palpiteId") ?: 0L
                val context = LocalContext.current
                val escopo = rememberCoroutineScope()

                TelaDetalhe(
                    uiState = uiState,
                    onVoltarClick = { navController.popBackStack() },
                    onEditarClick = viewModel::aoEntrarNoModoEdicao,
                    onRefazerClick = viewModel::aoRefazer,
                    onExcluirClick = viewModel::aoPedirExclusao,
                    onConfirmarExclusaoClick = {
                        viewModel.aoConfirmarExclusao()
                        navController.popBackStack()
                    },
                    onCancelarExclusaoClick = viewModel::aoCancelarExclusao,
                    onDezenaClick = viewModel::aoTocarDezenaNaEdicao,
                    onAlternarGuardarFixasClick = viewModel::aoAlternarGuardarComoFixas,
                    onCancelarEdicaoClick = viewModel::aoCancelarEdicao,
                    onSalvarEdicaoClick = viewModel::aoSalvarEdicao,
                    onLimparFixasClick = viewModel::aoLimparFixas,
                    onVerDesdobramentosClick = { navController.navigate(Rotas.desdobramentos(palpiteId)) },
                    onCompartilharClick = viewModel::aoAbrirCompartilharClick,
                    onFecharCompartilharClick = viewModel::aoFecharCompartilharClick,
                    onEnviarWhatsAppClick = { mensagem -> enviarPalpiteViaWhatsApp(context, mensagem) },
                    onCopiarTextoClick = { mensagem ->
                        copiarParaAreaDeTransferencia(context, mensagem)
                        viewModel.aoMarcarCopiadoClick()
                    },
                    onExportarClick = {
                        escopo.launch {
                            val uri =
                                gerarPdfDoVolante(context, uiState.dezenas, uiState.numeroDoDia, uiState.chanceRealUmEm)
                            abrirPdfParaCompartilhar(context, uri)
                        }
                    },
                    onExportarBloqueadoClick = { navController.navigate(Rotas.PAYWALL) },
                )
            }
            composable(
                Rotas.DESDOBRAMENTOS,
                arguments = listOf(navArgument("palpiteId") { type = NavType.LongType }),
            ) {
                val viewModel: DesdobramentosViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                TelaDesdobramentos(
                    uiState = uiState,
                    onVoltarClick = { navController.popBackStack() },
                )
            }
            composable(Rotas.PAYWALL) {
                val viewModel: PaywallViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                TelaPaywall(
                    uiState = uiState,
                    onFecharClick = { navController.popBackStack() },
                    onEscolherPlanoClick = viewModel::aoEscolherPlano,
                    onComecarTesteClick = {
                        context.comoActivity()?.let { activity -> viewModel.aoComecarTesteClick(activity) }
                    },
                )
            }
        }
    }
}

// RF-08.1: tenta abrir direto no WhatsApp (destaque do requisito); sem o
// app instalado, cai no seletor do sistema em vez de travar num app
// ausente.
private fun enviarPalpiteViaWhatsApp(
    context: Context,
    mensagem: String,
) {
    val intentWhatsApp =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensagem)
            setPackage("com.whatsapp")
        }
    try {
        context.startActivity(intentWhatsApp)
    } catch (excecao: ActivityNotFoundException) {
        val intentGenerico =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, mensagem)
            }
        context.startActivity(Intent.createChooser(intentGenerico, null))
    }
}

private fun copiarParaAreaDeTransferencia(
    context: Context,
    mensagem: String,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText(mensagem, mensagem))
}

// RF-09.2/09.4/09.7 — comprar e mostrar anúncio exigem Activity (não só
// Context) pro BillingClient/AdMob; LocalContext.current dentro do próprio
// setContent da Activity já É a Activity, mas desembrulha ContextWrapper
// pra não depender disso silenciosamente.
private tailrec fun Context.comoActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.comoActivity()
        else -> null
    }

// RF-07.8/RF-09.7 — gerenciar/cancelar assinatura é sempre na Play Store,
// nunca dentro do app (nenhuma via de pagamento própria, CLAUDE.md §1).
private fun abrirGerenciamentoDaAssinatura(
    context: Context,
    productId: String,
) {
    val uri =
        Uri.parse(
            "https://play.google.com/store/account/subscriptions?sku=$productId&package=${context.packageName}",
        )
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

// RF-08.3 — mesmo espírito do seletor de RF-08.1: abre o seletor do
// sistema em vez de forçar um app específico, já que "salvar"/"ver"/
// "enviar" um PDF são igualmente válidos aqui.
private fun abrirPdfParaCompartilhar(
    context: Context,
    uri: Uri,
) {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, null))
}
