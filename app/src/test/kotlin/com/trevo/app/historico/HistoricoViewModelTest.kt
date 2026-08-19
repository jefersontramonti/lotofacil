package com.trevo.app.historico

import com.trevo.app.MainDispatcherRule
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.app.resultado.FakeResultadoRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite
import com.trevo.core.engine.resultado.FaixaDePremio
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoricoViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val ZONA = ZoneId.of("America/Sao_Paulo")
        private val RELOGIO_HOJE: Clock = Clock.fixed(Instant.parse("2026-08-18T12:00:00Z"), ZONA)
    }

    private fun palpiteDeExemplo(dezenas: List<Int>) =
        Palpite(
            dezenas = dezenas,
            dezenasFixas = emptyList(),
            contribuicoes = mapOf(Crenca.SIGNO to listOf(1, 2, 3)),
            forca = 80,
        )

    private fun resultadoDoDia(
        dia: LocalDate,
        dezenasSorteadas: List<Int>,
        numero: Int = 3457,
    ) = Resultado(
        numero = numero,
        dataApuracao = dia,
        dezenasSorteadas = dezenasSorteadas,
        faixasDePremio =
            listOf(
                FaixaDePremio(acertosNecessarios = 15, numeroDeGanhadores = 1, valorPremio = BigDecimal("1500.00")),
            ),
        acumulado = false,
        origem = OrigemDoResultado.API,
    )

    private fun relogioNoDia(dia: LocalDate): Clock =
        Clock.fixed(dia.atStartOfDay(ZONA).plusHours(12).toInstant(), ZONA)

    private fun novoViewModel(
        palpiteRepository: FakePalpiteRepository,
        resultadoRepository: FakeResultadoRepository,
    ) = HistoricoViewModel(
        palpiteRepository = palpiteRepository,
        resultadoRepository = resultadoRepository,
        clock = RELOGIO_HOJE,
    )

    @Test
    fun semPalpitesDevolveVazio() =
        runTest {
            val palpiteRepository = FakePalpiteRepository(RELOGIO_HOJE)
            val resultadoRepository = FakeResultadoRepository(RELOGIO_HOJE)
            val viewModel = novoViewModel(palpiteRepository, resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is HistoricoUiState.Vazio)
        }

    @Test
    fun palpiteSemResultadoCasadoDevolveVazio() =
        runTest {
            val dia = LocalDate.of(2026, 8, 17)
            val palpiteRepository = FakePalpiteRepository(relogioNoDia(dia))
            palpiteRepository.salvar(palpiteDeExemplo((1..15).toList()))
            val resultadoRepository = FakeResultadoRepository(RELOGIO_HOJE)
            val viewModel = novoViewModel(palpiteRepository, resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is HistoricoUiState.Vazio)
        }

    @Test
    fun umConcursoConferidoTrazAgregadosCorretos() =
        runTest {
            val dia = LocalDate.of(2026, 8, 17)
            val palpiteRepository = FakePalpiteRepository(relogioNoDia(dia))
            palpiteRepository.salvar(palpiteDeExemplo((1..15).toList()))
            val resultadoRepository = FakeResultadoRepository(RELOGIO_HOJE)
            resultadoRepository.adicionarResultado(resultadoDoDia(dia, (1..15).toList()))
            val viewModel = novoViewModel(palpiteRepository, resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val estado = viewModel.uiState.value as HistoricoUiState.ComDados
            assertEquals(1, estado.totalDeJogos)
            assertEquals(1, estado.totalDeConcursos)
            assertEquals(0, BigDecimal("3.50").compareTo(estado.totalGasto))
            assertEquals(0, BigDecimal("1500.00").compareTo(estado.totalGanho))
            assertEquals(15, estado.melhorResultadoEmAcertos)
            assertEquals(1, estado.faixas.first { it.acertos == 15 }.quantidade)
            assertEquals(1, estado.concursosRevelados.size)
        }

    @Test
    fun saldoERetornoBatemComGastoEGanho() =
        runTest {
            val dia = LocalDate.of(2026, 8, 17)
            val palpiteRepository = FakePalpiteRepository(relogioNoDia(dia))
            // 10 acertos: sem faixa premiada (só 15 configurada acima) — gasta sem ganhar nada.
            palpiteRepository.salvar(palpiteDeExemplo((1..15).toList()))
            val resultadoRepository = FakeResultadoRepository(RELOGIO_HOJE)
            resultadoRepository.adicionarResultado(resultadoDoDia(dia, (1..10).toList() + (16..20).toList()))
            val viewModel = novoViewModel(palpiteRepository, resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val estado = viewModel.uiState.value as HistoricoUiState.ComDados
            assertEquals(0, BigDecimal("0.00").compareTo(estado.totalGanho))
            assertEquals(0, BigDecimal("-3.50").compareTo(estado.saldo))
            assertEquals(0, estado.retornoPercentual)
        }

    @Test
    fun paginacaoRevelaTresPorVezEAoVerMaisAumentaEmTres() =
        runTest {
            val palpiteRepository = FakePalpiteRepository(RELOGIO_HOJE)
            val resultadoRepository = FakeResultadoRepository(RELOGIO_HOJE)
            (1..5).forEach { deslocamento ->
                val dia = LocalDate.of(2026, 8, 17).minusDays(deslocamento.toLong())
                val criadoEm = dia.atStartOfDay(ZONA).plusHours(12).toInstant()
                palpiteRepository.salvarComData(palpiteDeExemplo((1..15).toList()), criadoEm)
                resultadoRepository.adicionarResultado(
                    resultadoDoDia(
                        dia,
                        (1..15).toList(),
                        numero =
                            3450 + deslocamento,
                    ),
                )
            }
            val viewModel = novoViewModel(palpiteRepository, resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            var estado = viewModel.uiState.value as HistoricoUiState.ComDados
            assertEquals(5, estado.totalDeConcursos)
            assertEquals(3, estado.concursosRevelados.size)
            assertTrue(estado.temMaisConcursos)
            assertEquals(2, estado.quantidadeDeConcursosRestantes)

            viewModel.aoVerMaisClick()
            advanceUntilIdle()

            estado = viewModel.uiState.value as HistoricoUiState.ComDados
            assertEquals(5, estado.concursosRevelados.size)
            assertTrue(!estado.temMaisConcursos)
        }
}
