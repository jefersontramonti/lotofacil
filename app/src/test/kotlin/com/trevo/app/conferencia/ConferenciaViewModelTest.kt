package com.trevo.app.conferencia

import com.trevo.app.MainDispatcherRule
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.app.resultado.FakeResultadoRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite
import com.trevo.core.engine.resultado.FaixaDePremio
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ConferenciaViewModelTest {
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
            contribuicoes = mapOf(Crenca.SIGNO to listOf(1, 2, 3)),
            forca = 80,
        )

    private val resultadoDeHoje =
        Resultado(
            numero = 3457,
            dataApuracao = java.time.LocalDate.now(RELOGIO_FIXO),
            dezenasSorteadas = (1..15).toList(),
            faixasDePremio =
                listOf(
                    FaixaDePremio(
                        acertosNecessarios = 15,
                        numeroDeGanhadores = 10,
                        valorPremio = BigDecimal("1500.00"),
                    ),
                ),
            acumulado = false,
            origem = OrigemDoResultado.API,
            proximoConcurso = null,
        )

    private fun novoViewModel(
        resultadoRepository: FakeResultadoRepository = FakeResultadoRepository(RELOGIO_FIXO),
        palpiteRepository: FakePalpiteRepository = FakePalpiteRepository(RELOGIO_FIXO),
    ) = ConferenciaViewModel(
        resultadoRepository = resultadoRepository,
        palpiteRepository = palpiteRepository,
        clock = RELOGIO_FIXO,
    )

    @Test
    fun estadoInicialECarregando() {
        val viewModel = novoViewModel()
        assertTrue(viewModel.uiState.value is ConferenciaUiState.Carregando)
    }

    @Test
    fun semPalpitesHojeEComResultadoDevolveSucessoComItensVazios() =
        runTest {
            val resultadoRepository = FakeResultadoRepository(RELOGIO_FIXO).apply { proximoResultado = resultadoDeHoje }
            val viewModel = novoViewModel(resultadoRepository = resultadoRepository)

            viewModel.aoEntrar()
            advanceUntilIdle()

            val estado = viewModel.uiState.value
            assertTrue(estado is ConferenciaUiState.Sucesso)
            assertTrue((estado as ConferenciaUiState.Sucesso).itens.isEmpty())
        }

    @Test
    fun resultadoAnteriorAHojeComPalpitesHojeDevolveEspera() =
        runTest {
            val resultadoAntigo =
                resultadoDeHoje.copy(
                    dataApuracao =
                        java.time.LocalDate
                            .now(RELOGIO_FIXO)
                            .minusDays(1),
                )
            val resultadoRepository = FakeResultadoRepository(RELOGIO_FIXO).apply { proximoResultado = resultadoAntigo }
            val palpiteRepository = FakePalpiteRepository(RELOGIO_FIXO)
            palpiteRepository.salvar(palpiteDeExemplo)
            val viewModel =
                novoViewModel(resultadoRepository = resultadoRepository, palpiteRepository = palpiteRepository)

            viewModel.aoEntrar()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is ConferenciaUiState.Espera)
        }

    @Test
    fun palpiteComQuinzeAcertosTrazAFaixaEOPremioCorretos() =
        runTest {
            val resultadoRepository = FakeResultadoRepository(RELOGIO_FIXO).apply { proximoResultado = resultadoDeHoje }
            val palpiteRepository = FakePalpiteRepository(RELOGIO_FIXO)
            palpiteRepository.salvar(palpiteDeExemplo)
            val viewModel =
                novoViewModel(resultadoRepository = resultadoRepository, palpiteRepository = palpiteRepository)

            viewModel.aoEntrar()
            advanceUntilIdle()

            val estado = viewModel.uiState.value as ConferenciaUiState.Sucesso
            assertEquals(1, estado.itens.size)
            val item = estado.itens.first()
            assertEquals(15, item.acertos)
            assertEquals(0, BigDecimal("1500.00").compareTo(item.premio))
            assertEquals(0, BigDecimal("1500.00").compareTo(estado.totalGanho))
        }

    @Test
    fun falhaDeRedeDevolveSemConexao() =
        runTest {
            val resultadoRepository =
                FakeResultadoRepository(RELOGIO_FIXO).apply {
                    proximaExcecao =
                        IOException("sem rede")
                }
            val viewModel = novoViewModel(resultadoRepository = resultadoRepository)

            viewModel.aoEntrar()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is ConferenciaUiState.SemConexao)
        }

    @Test
    fun falhaDoServicoDevolveFalha() =
        runTest {
            val resultadoRepository =
                FakeResultadoRepository(RELOGIO_FIXO).apply { proximaExcecao = IllegalStateException("erro 500") }
            val viewModel = novoViewModel(resultadoRepository = resultadoRepository)

            viewModel.aoEntrar()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is ConferenciaUiState.Falha)
        }

    @Test
    fun tentarNovamenteRefazABusca() =
        runTest {
            val resultadoRepository =
                FakeResultadoRepository(RELOGIO_FIXO).apply { proximaExcecao = IOException("sem rede") }
            val viewModel = novoViewModel(resultadoRepository = resultadoRepository)
            viewModel.aoEntrar()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ConferenciaUiState.SemConexao)

            resultadoRepository.proximaExcecao = null
            resultadoRepository.proximoResultado = resultadoDeHoje
            viewModel.aoTentarNovamente()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is ConferenciaUiState.Sucesso)
        }

    @Test
    fun informarResultadoManualmenteSalvaEMostraSucessoSemPremio() =
        runTest {
            val resultadoRepository =
                FakeResultadoRepository(RELOGIO_FIXO).apply { proximaExcecao = IOException("sem rede") }
            val palpiteRepository = FakePalpiteRepository(RELOGIO_FIXO)
            palpiteRepository.salvar(palpiteDeExemplo)
            val viewModel =
                novoViewModel(resultadoRepository = resultadoRepository, palpiteRepository = palpiteRepository)
            viewModel.aoEntrar()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ConferenciaUiState.SemConexao)

            viewModel.aoInformarResultadoManualmente((1..15).toSet())
            advanceUntilIdle()

            val estado = viewModel.uiState.value as ConferenciaUiState.Sucesso
            assertTrue(estado.origemManual)
            assertNull(estado.numeroDoConcurso)
            assertNull(estado.itens.first().premio)
        }
}
