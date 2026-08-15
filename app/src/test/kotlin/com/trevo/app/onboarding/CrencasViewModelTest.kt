package com.trevo.app.onboarding

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.palpite.PalpiteGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

class CrencasViewModelTest {
    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-13T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private fun novoViewModel(semente: Int = 1) =
        CrencasViewModel(
            gerador = PalpiteGenerator(Random(semente)),
            validadorDeNascimento = ValidadorDataNascimento(RELOGIO_FIXO),
            clock = RELOGIO_FIXO,
        )

    @Test
    fun estadoInicialNaoTemCrencaSelecionada() {
        val viewModel = novoViewModel()

        assertTrue(
            viewModel.uiState.value.selecionadas
                .isEmpty(),
        )
    }

    @Test
    fun estadoInicialNaoTemPalpiteGerado() {
        val viewModel = novoViewModel()

        assertNull(viewModel.uiState.value.palpiteGerado)
    }

    @Test
    fun tocarUmaCrencaNaoSelecionadaAdicionaAsSelecionadas() {
        val viewModel = novoViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)

        assertEquals(setOf(Crenca.SIGNO), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun tocarDeNovoUmaCrencaJaSelecionadaRemoveDasSelecionadas() {
        val viewModel = novoViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)
        viewModel.aoTocarCrenca(Crenca.SIGNO)

        assertTrue(
            viewModel.uiState.value.selecionadas
                .isEmpty(),
        )
    }

    @Test
    fun tocarVariasCrencasAcumulaTodasNaSelecao() {
        val viewModel = novoViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)
        viewModel.aoTocarCrenca(Crenca.LUA)
        viewModel.aoTocarCrenca(Crenca.SONHO)

        assertEquals(setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun removerUmaCrencaNaoAlteraAsOutrasJaSelecionadas() {
        val viewModel = novoViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)
        viewModel.aoTocarCrenca(Crenca.LUA)
        viewModel.aoTocarCrenca(Crenca.SIGNO)

        assertEquals(setOf(Crenca.LUA), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun todasAs12CrencasPodemSerSelecionadasAoMesmoTempo() {
        val viewModel = novoViewModel()

        Crenca.entries.forEach(viewModel::aoTocarCrenca)

        assertEquals(Crenca.entries.toSet(), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun gerarPalpiteProduz15DezenasEPreencheOEstado() {
        val viewModel = novoViewModel()

        viewModel.aoGerarPalpite(nome = "Marlene", nascimentoTexto = "14/07/1978", signo = Signo.CANCER)

        val palpite = viewModel.uiState.value.palpiteGerado
        assertEquals(15, palpite?.dezenas?.size)
    }

    @Test
    fun gerarPalpiteUsaAsCrencasSelecionadasNoMomentoDoClique() {
        val viewModel = novoViewModel()
        viewModel.aoTocarCrenca(Crenca.SIGNO)

        viewModel.aoGerarPalpite(nome = "Marlene", nascimentoTexto = "14/07/1978", signo = Signo.CANCER)

        val contribuicoes =
            viewModel.uiState.value.palpiteGerado
                ?.contribuicoes
        assertTrue(contribuicoes?.containsKey(Crenca.SIGNO) == true)
    }

    @Test
    fun gerarPalpiteComNascimentoInvalidoNaoQuebraEIgnoraACrencaDeNascimento() {
        val viewModel = novoViewModel()
        viewModel.aoTocarCrenca(Crenca.NASCIMENTO)

        viewModel.aoGerarPalpite(nome = "Marlene", nascimentoTexto = "31/04/1978", signo = null)

        val contribuicoes =
            viewModel.uiState.value.palpiteGerado
                ?.contribuicoes
        assertEquals(emptyList<Int>(), contribuicoes?.get(Crenca.NASCIMENTO))
    }

    @Test
    fun gerarPalpiteDuasVezesComAMesmaSelecaoSubstituiOResultadoAnterior() {
        val viewModel = novoViewModel()

        viewModel.aoGerarPalpite(nome = "Marlene", nascimentoTexto = "14/07/1978", signo = Signo.CANCER)
        val primeiroPalpite = viewModel.uiState.value.palpiteGerado

        viewModel.aoGerarPalpite(nome = "Marlene", nascimentoTexto = "14/07/1978", signo = Signo.CANCER)
        val segundoPalpite = viewModel.uiState.value.palpiteGerado

        assertTrue(primeiroPalpite != null && segundoPalpite != null)
    }
}
