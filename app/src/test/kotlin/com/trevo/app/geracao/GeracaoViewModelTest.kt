package com.trevo.app.geracao

import com.trevo.app.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * RF-02.9 — "Exibir animação de ritual durante a geração, com no mínimo
 * três frases sequenciais." O wireframe 1f usa quatro; a espera é encenação
 * ("o tempo de espera é o ritual, não latência real"), então o teste
 * verifica a sequência e a conclusão, não valores de tempo exatos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GeracaoViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    @Test
    fun estadoInicialComecaNaPrimeiraFraseENaoConcluido() {
        val viewModel = GeracaoViewModel()

        assertEquals(0, viewModel.uiState.value.indiceFrase)
        assertFalse(viewModel.uiState.value.concluido)
    }

    @Test
    fun iniciarPercorreAsQuatroFrasesEmSequenciaAteConcluir() =
        runTest {
            val viewModel = GeracaoViewModel()

            viewModel.iniciar(movimentoReduzido = false)
            val indicesVistos = mutableListOf(viewModel.uiState.value.indiceFrase)
            repeat(QUANTIDADE_DE_FRASES_DO_RITUAL - 1) {
                advanceTimeBy(700)
                indicesVistos += viewModel.uiState.value.indiceFrase
            }
            assertFalse(viewModel.uiState.value.concluido)

            advanceUntilIdle()

            assertEquals((0 until QUANTIDADE_DE_FRASES_DO_RITUAL).toList(), indicesVistos)
            assertTrue(viewModel.uiState.value.concluido)
        }

    @Test
    fun chamarIniciarDeNovoNaoReiniciaOCiclo() =
        runTest {
            val viewModel = GeracaoViewModel()

            viewModel.iniciar(movimentoReduzido = false)
            advanceUntilIdle()
            val estadoAposPrimeiraConclusao = viewModel.uiState.value

            viewModel.iniciar(movimentoReduzido = false)
            advanceUntilIdle()

            assertEquals(estadoAposPrimeiraConclusao, viewModel.uiState.value)
        }

    @Test
    fun comMovimentoReduzidoConcluiMuitoMaisRapidoDoQueNoRitmoPadrao() =
        runTest {
            val viewModel = GeracaoViewModel()

            viewModel.iniciar(movimentoReduzido = true)
            advanceTimeBy(300)

            assertTrue(viewModel.uiState.value.concluido)
        }

    @Test
    fun semMovimentoReduzidoAindaNaoConcluiEmTempoQueSoBastaParaOModoReduzido() =
        runTest {
            val viewModel = GeracaoViewModel()

            viewModel.iniciar(movimentoReduzido = false)
            advanceTimeBy(300)

            assertFalse(viewModel.uiState.value.concluido)
        }
}
