package com.trevo.app.navegacao

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trevo.app.detalhe.DesdobramentosViewModel
import com.trevo.app.detalhe.DetalheViewModel
import com.trevo.app.detalhe.TelaDesdobramentos
import com.trevo.app.detalhe.TelaDetalhe
import com.trevo.app.geracao.GeracaoViewModel
import com.trevo.app.geracao.TelaGerando
import com.trevo.app.geracao.movimentoReduzidoAtivado
import com.trevo.app.home.HomeViewModel
import com.trevo.app.home.TelaHome
import com.trevo.app.onboarding.CrencasViewModel
import com.trevo.app.onboarding.IdentidadeViewModel
import com.trevo.app.onboarding.TelaAbertura
import com.trevo.app.onboarding.TelaCrencas
import com.trevo.app.onboarding.TelaIdentidade

@Composable
fun TrevoNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rotas.ABERTURA,
        modifier = modifier,
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
                onCrencaBloqueadaClick = {
                    // RF-01.8/RF-09 (paywall) registram o destino aqui
                },
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
            )
        }
        composable(
            Rotas.DETALHE,
            arguments = listOf(navArgument("palpiteId") { type = NavType.LongType }),
        ) { entrada ->
            val viewModel: DetalheViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val palpiteId = entrada.arguments?.getLong("palpiteId") ?: 0L

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
    }
}
