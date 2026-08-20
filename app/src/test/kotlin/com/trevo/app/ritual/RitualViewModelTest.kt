package com.trevo.app.ritual

import com.trevo.app.MainDispatcherRule
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.app.preferencias.FakePreferenciasRepository
import com.trevo.core.engine.crenca.Amuleto
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.ORDEM_DO_RITUAL
import com.trevo.core.engine.crenca.OpcaoDeAmuleto
import com.trevo.core.engine.crenca.opcoesDoAmuleto
import com.trevo.core.engine.palpite.PalpiteGenerator
import com.trevo.core.engine.palpite.TamanhoDeFechamento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class RitualViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private fun novoAmbiente(
        preferencias: FakePreferenciasRepository = FakePreferenciasRepository(),
        palpites: FakePalpiteRepository = FakePalpiteRepository(RELOGIO_FIXO),
        semente: Int = 1,
    ) = Triple(
        RitualViewModel(
            gerador = PalpiteGenerator(Random(semente)),
            preferenciasRepository = preferencias,
            palpiteRepository = palpites,
            clock = RELOGIO_FIXO,
        ),
        preferencias,
        palpites,
    )

    @Test
    fun estadoInicialEEscolhaDoPrimeiroAmuletoAposCarregar() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", LocalDate.of(1978, 7, 14), null, setOf(Crenca.SIGNO))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val estado = viewModel.uiState.value as RitualUiState.Escolha
            assertEquals(Amuleto.TREVO, estado.amuletoAtual)
            assertEquals(1, estado.indice)
            assertEquals(ORDEM_DO_RITUAL.size, estado.total)
            assertTrue(estado.reveladas.isEmpty())
        }

    @Test
    fun escolherUmaOpcaoRevelaUmaDezenaEntre1E25() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoEscolherOpcao(OpcaoDeAmuleto.TREVO_SORTE)
            advanceUntilIdle()

            val estado = viewModel.uiState.value as RitualUiState.Revelando
            assertEquals(Amuleto.TREVO, estado.ultimaRevelacao.amuleto)
            assertEquals(OpcaoDeAmuleto.TREVO_SORTE, estado.ultimaRevelacao.opcao)
            assertTrue(estado.ultimaRevelacao.dezena in 1..25)
        }

    @Test
    fun terminarARevelacaoAvancaParaOProximoAmuleto() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoEscolherOpcao(OpcaoDeAmuleto.TREVO_SORTE)
            advanceUntilIdle()

            viewModel.aoRevelacaoTerminou()
            advanceUntilIdle()

            val estado = viewModel.uiState.value as RitualUiState.Escolha
            assertEquals(Amuleto.FERRADURA, estado.amuletoAtual)
            assertEquals(2, estado.indice)
            assertEquals(1, estado.reveladas.size)
        }

    @Test
    fun completarOsOitoAmuletosLevaAoResumoComOitoRevelacoesENuncaRepeteDezena() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente(semente = 7)
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }

            val estado = viewModel.uiState.value as RitualUiState.Resumo
            assertEquals(ORDEM_DO_RITUAL.size, estado.reveladas.size)
            assertEquals(15 - ORDEM_DO_RITUAL.size, estado.quantidadeDeOutrasDezenas)
            assertEquals(
                ORDEM_DO_RITUAL.size,
                estado.reveladas
                    .map { it.dezena }
                    .toSet()
                    .size,
            )
        }

    @Test
    fun refazerRitualNoResumoVoltaParaOPrimeiroAmuletoSemReveladas() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }
            assertTrue(viewModel.uiState.value is RitualUiState.Resumo)

            viewModel.aoRefazerRitualClick()
            advanceUntilIdle()

            val estado = viewModel.uiState.value as RitualUiState.Escolha
            assertEquals(Amuleto.TREVO, estado.amuletoAtual)
            assertTrue(estado.reveladas.isEmpty())
        }

    @Test
    fun montarPalpiteSalvaComModoDestinoRitualEDezenasForcadas() =
        runTest {
            val (viewModel, preferencias, palpites) = novoAmbiente(semente = 3)
            preferencias.salvarPerfil("Marlene", LocalDate.of(1978, 7, 14), null, setOf(Crenca.SIGNO))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }
            val reveladas = (viewModel.uiState.value as RitualUiState.Resumo).reveladas

            viewModel.aoMontarPalpiteClick()
            advanceUntilIdle()

            assertEquals(1, palpites.todos.value.size)
            val palpiteSalvo =
                palpites.todos.value
                    .first()
                    .palpite
            assertEquals(ModoDeGeracao.DESTINO, palpiteSalvo.modo)
            assertEquals(reveladas, palpiteSalvo.ritual)
            assertEquals(15, palpiteSalvo.dezenas.size)
            assertTrue(reveladas.map { it.dezena }.all { it in palpiteSalvo.dezenas })
        }

    @Test
    fun montarPalpiteEmiteEventoDePalpiteMontado() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }

            viewModel.aoMontarPalpiteClick()
            advanceUntilIdle()

            // O evento já foi enviado ao canal (buffered) por aoMontarPalpiteClick
            // antes deste advanceUntilIdle() — first() só drena o que já está lá.
            assertEquals(RitualEvento.PalpiteMontado, viewModel.eventos.first())
        }

    @Test
    fun escolherTamanhoDeFechamentoAtualizaQuantidadeDeOutrasDezenas() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }
            assertEquals(7, (viewModel.uiState.value as RitualUiState.Resumo).quantidadeDeOutrasDezenas)

            viewModel.aoEscolherTamanho(TamanhoDeFechamento.DEZESSEIS)
            advanceUntilIdle()

            val estado = viewModel.uiState.value as RitualUiState.Resumo
            assertEquals(TamanhoDeFechamento.DEZESSEIS, estado.tamanho)
            assertEquals(8, estado.quantidadeDeOutrasDezenas)
        }

    @Test
    fun escolherTamanhoNuloVoltaAoPadraoDe15() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }
            viewModel.aoEscolherTamanho(TamanhoDeFechamento.VINTE)
            advanceUntilIdle()

            viewModel.aoEscolherTamanho(null)
            advanceUntilIdle()

            val estado = viewModel.uiState.value as RitualUiState.Resumo
            assertEquals(null, estado.tamanho)
            assertEquals(7, estado.quantidadeDeOutrasDezenas)
        }

    @Test
    fun montarPalpiteComFechamentoGeraOTamanhoEscolhidoComAsDezenasDoRitualForcadas() =
        runTest {
            val (viewModel, preferencias, palpites) = novoAmbiente(semente = 5)
            preferencias.salvarPerfil("Marlene", LocalDate.of(1978, 7, 14), null, setOf(Crenca.SIGNO))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            ORDEM_DO_RITUAL.forEach { amuleto ->
                viewModel.aoEscolherOpcao(opcoesDoAmuleto(amuleto).first())
                advanceUntilIdle()
                viewModel.aoRevelacaoTerminou()
                advanceUntilIdle()
            }
            val reveladas = (viewModel.uiState.value as RitualUiState.Resumo).reveladas
            viewModel.aoEscolherTamanho(TamanhoDeFechamento.VINTE)
            advanceUntilIdle()

            viewModel.aoMontarPalpiteClick()
            advanceUntilIdle()

            val palpiteSalvo =
                palpites.todos.value
                    .first()
                    .palpite
            assertEquals(20, palpiteSalvo.dezenas.size)
            assertTrue(reveladas.map { it.dezena }.all { it in palpiteSalvo.dezenas })
        }
}
