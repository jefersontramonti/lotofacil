package com.trevo.app.detalhe

import androidx.lifecycle.SavedStateHandle
import com.trevo.app.MainDispatcherRule
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DesdobramentosViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-17T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private fun novoViewModel(
        palpiteId: Long,
        repository: FakePalpiteRepository,
    ) = DesdobramentosViewModel(
        savedStateHandle = SavedStateHandle(mapOf("palpiteId" to palpiteId)),
        repository = repository,
    )

    @Test
    fun umFechamentoDe18BateComATabelaOficialDeJogosECusto() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val palpite =
                Palpite(
                    dezenas = (1..18).toList(),
                    dezenasFixas = emptyList(),
                    contribuicoes = mapOf(Crenca.SIGNO to emptyList()),
                    forca = 100,
                )
            val id = repository.salvar(palpite)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)

            advanceUntilIdle()

            assertEquals(18, viewModel.uiState.value.quantidadeDeDezenas)
            assertEquals(816L, viewModel.uiState.value.jogosEquivalentes)
            assertEquals(0, BigDecimal("2856.00").compareTo(viewModel.uiState.value.custoTotal))
        }

    @Test
    fun listaDeCombinacoesFicaLimitadaA24MesmoComMuitasPossiveis() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val palpite =
                Palpite(
                    dezenas = (1..20).toList(),
                    dezenasFixas = emptyList(),
                    contribuicoes = emptyMap(),
                    forca = 100,
                )
            val id = repository.salvar(palpite)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)

            advanceUntilIdle()

            assertEquals(24, viewModel.uiState.value.combinacoesExibidas.size)
            assertEquals(
                24,
                viewModel.uiState.value.combinacoesExibidas
                    .toSet()
                    .size,
            )
        }

    @Test
    fun umFechamentoDe15MostraTodasAsCombinacoesPoisSoExisteUma() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val palpite =
                Palpite(dezenas = (1..15).toList(), dezenasFixas = emptyList(), contribuicoes = emptyMap(), forca = 100)
            val id = repository.salvar(palpite)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)

            advanceUntilIdle()

            assertEquals(1L, viewModel.uiState.value.jogosEquivalentes)
            assertEquals(listOf((1..15).toList()), viewModel.uiState.value.combinacoesExibidas)
        }
}
