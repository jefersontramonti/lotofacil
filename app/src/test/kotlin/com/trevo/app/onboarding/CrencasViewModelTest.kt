package com.trevo.app.onboarding

import com.trevo.core.engine.crenca.Crenca
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrencasViewModelTest {
    @Test
    fun estadoInicialNaoTemCrencaSelecionada() {
        val viewModel = CrencasViewModel()

        assertTrue(
            viewModel.uiState.value.selecionadas
                .isEmpty(),
        )
    }

    @Test
    fun tocarUmaCrencaNaoSelecionadaAdicionaAsSelecionadas() {
        val viewModel = CrencasViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)

        assertEquals(setOf(Crenca.SIGNO), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun tocarDeNovoUmaCrencaJaSelecionadaRemoveDasSelecionadas() {
        val viewModel = CrencasViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)
        viewModel.aoTocarCrenca(Crenca.SIGNO)

        assertTrue(
            viewModel.uiState.value.selecionadas
                .isEmpty(),
        )
    }

    @Test
    fun tocarVariasCrencasAcumulaTodasNaSelecao() {
        val viewModel = CrencasViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)
        viewModel.aoTocarCrenca(Crenca.LUA)
        viewModel.aoTocarCrenca(Crenca.SONHO)

        assertEquals(setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun removerUmaCrencaNaoAlteraAsOutrasJaSelecionadas() {
        val viewModel = CrencasViewModel()

        viewModel.aoTocarCrenca(Crenca.SIGNO)
        viewModel.aoTocarCrenca(Crenca.LUA)
        viewModel.aoTocarCrenca(Crenca.SIGNO)

        assertEquals(setOf(Crenca.LUA), viewModel.uiState.value.selecionadas)
    }

    @Test
    fun todasAs12CrencasPodemSerSelecionadasAoMesmoTempo() {
        val viewModel = CrencasViewModel()

        Crenca.entries.forEach(viewModel::aoTocarCrenca)

        assertEquals(Crenca.entries.toSet(), viewModel.uiState.value.selecionadas)
    }
}
