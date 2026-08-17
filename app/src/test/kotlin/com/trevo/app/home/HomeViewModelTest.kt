package com.trevo.app.home

import com.trevo.app.MainDispatcherRule
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-17T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private val palpiteDeExemplo =
        Palpite(
            dezenas = (1..15).toList(),
            dezenasFixas = emptyList(),
            contribuicoes = emptyMap(),
            forca = 70,
        )

    private fun novoViewModel(repository: FakePalpiteRepository) = HomeViewModel(repository, RELOGIO_FIXO)

    @Test
    fun estadoComecaSemPalpitesQuandoORepositorioEstaVazio() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.palpitesHoje
                    .isEmpty(),
            )
        }

    @Test
    fun palpiteSalvoNoRepositorioApareceNaListaDoDia() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
            assertEquals(
                palpiteDeExemplo.dezenas,
                viewModel.uiState.value.palpitesHoje
                    .first()
                    .dezenas,
            )
        }

    @Test
    fun pedirExclusaoNaoRemoveOPalpiteAteConfirmar() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            val id = repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()

            viewModel.aoPedirExclusao(id)
            advanceUntilIdle()

            assertEquals(id, viewModel.uiState.value.palpiteParaConfirmarExclusao)
            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
        }

    @Test
    fun confirmarExclusaoRemoveOPalpiteELimpaOPedido() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            val id = repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()
            viewModel.aoPedirExclusao(id)

            viewModel.aoConfirmarExclusao()
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.palpitesHoje
                    .isEmpty(),
            )
            assertNull(viewModel.uiState.value.palpiteParaConfirmarExclusao)
        }

    @Test
    fun cancelarExclusaoLimpaOPedidoSemRemoverOPalpite() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            val id = repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()
            viewModel.aoPedirExclusao(id)

            viewModel.aoCancelarExclusao()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.palpiteParaConfirmarExclusao)
            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
        }

    @Test
    fun numeroDoDiaCresceComAOrdemDeCriacaoMasListaVemDoMaisRecenteAoMaisAntigo() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.salvar(palpiteDeExemplo)
            repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()

            val numeros =
                viewModel.uiState.value.palpitesHoje
                    .map { it.numeroDoDia }
            assertEquals(listOf(2, 1), numeros)
        }
}
