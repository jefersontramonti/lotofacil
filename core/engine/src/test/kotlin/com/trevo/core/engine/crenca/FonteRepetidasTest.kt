package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteRepetidasTest {
    private val fonte = FonteRepetidas()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semHistoricoDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = emptyList()))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun devolveAsDezenasDoConcursoMaisRecenteOrdenadas() {
        val historico = listOf(listOf(20, 5, 15), listOf(1, 2, 3))

        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = historico))

        assertEquals(listOf(5, 15, 20), contribuicao.dezenas)
    }
}
