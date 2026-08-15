package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteQuentesTest {
    private val fonte = FonteQuentes()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semHistoricoDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = emptyList()))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun devolveAsOitoMaisFrequentesDesempatandoPelaMenorDezena() {
        val historico =
            listOf(
                listOf(1, 2, 3),
                listOf(1, 2, 4),
                listOf(1, 5),
            )
        // Frequência: 1x3, 2x2, 3x1, 4x1, 5x1. As demais (6..25) têm 0.
        // Top 8 por frequência desc., empate pela menor dezena: 1,2,3,4,5,6,7,8.
        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = historico))

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), contribuicao.dezenas)
    }

    @Test
    fun usaSoOsUltimos50ConcursosDoHistorico() {
        val concursoAntigo = List(30) { 20 }
        val historico = listOf(listOf(1)) + List(50) { listOf(1) } + listOf(concursoAntigo)

        val contribuicao = fonte.contribuir(dadosBase.copy(historicoDeConcursos = historico))

        // A dezena 20 só aparece no concurso 52º mais antigo, fora da
        // janela de 50 — não deveria ser a mais frequente.
        assertTrue(contribuicao.dezenas.first() == 1)
    }
}
