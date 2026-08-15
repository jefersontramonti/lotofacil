package com.trevo.app.navegacao

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        composable(Rotas.CRENCAS) {
            val viewModel: CrencasViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            TelaCrencas(
                uiState = uiState,
                onCrencaClick = viewModel::aoTocarCrenca,
                onVoltarClick = { navController.popBackStack() },
                onContinuarClick = {
                    // RF-03 (home) registra a próxima rota aqui
                },
            )
        }
    }
}
