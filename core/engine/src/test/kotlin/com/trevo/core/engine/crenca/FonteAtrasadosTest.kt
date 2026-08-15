package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteAtrasadosTest {
    private val fonte = FonteAtrasados()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semHistoricoDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = emptyList()))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun comMenosDeSeisConcursosDevolveListaVaziaEMotivo() {
        val historico = List(5) { (1..15).toList() }

        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = historico))

        assertTrue(contribuicao.dezenas.isEmpty())
    }

    @Test
    fun devolveAsDezenasAusentesEmTodosOsUltimos6Concursos() {
        // As mesmas 15 dezenas saem em todos os 6 concursos; as outras
        // 10 (16..25) nunca saem — são as atrasadas.
        val historico = List(6) { (1..15).toList() }

        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = historico))

        assertEquals((16..25).toList(), contribuicao.dezenas)
    }

    @Test
    fun usaSoOsUltimos6ConcursosMesmoComMaisNoHistorico() {
        val seisRecentesComTudo = List(6) { (1..25).toList() }
        val historico = seisRecentesComTudo + listOf((1..10).toList())

        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = historico))

        assertTrue(contribuicao.dezenas.isEmpty())
    }
}
