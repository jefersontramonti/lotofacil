package com.trevo.app.navegacao

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                    // O palpite já é gerado com as crenças ativas, pronto
                    // pra quando a tela de destino (RF-03/home) existir —
                    // só não é exibido nesta tela (wireframe 1c: o CTA
                    // leva pra dentro do app, não mostra resultado aqui).
                    viewModel.aoGerarPalpite(
                        nome = identidadeUiState.nome,
                        nascimentoTexto = identidadeUiState.nascimento,
                        signo = identidadeUiState.signo,
                    )
                },
            )
        }
    }
}
