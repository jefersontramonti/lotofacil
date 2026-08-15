package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteSonhoTest {
    private val fonte = FonteSonho()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semGrupoDoSonhoDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(grupoDoSonho = null))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun grupoForaDoIntervaloDevolveListaVaziaSemLancarExcecao() {
        val contribuicao = fonte.contribuir(dadosBase.copy(grupoDoSonho = 26))

        assertTrue(contribuicao.dezenas.isEmpty())
    }

    @Test
    fun grupo13DevolveUmaUnicaDezena() {
        val contribuicao = fonte.contribuir(dadosBase.copy(grupoDoSonho = 13))

        assertEquals(listOf(13), contribuicao.dezenas)
    }

    @Test
    fun grupoValidoDevolveADezenaEAEspelhada() {
        val contribuicao = fonte.contribuir(dadosBase.copy(grupoDoSonho = 9))

        assertEquals(listOf(9, 17), contribuicao.dezenas)
    }
}
