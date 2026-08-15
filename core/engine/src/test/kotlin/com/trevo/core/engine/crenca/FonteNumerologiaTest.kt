package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteNumerologiaTest {
    private val fonte = FonteNumerologia()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semNomeDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(nome = null))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun nomeEmBrancoDevolveListaVazia() {
        assertTrue(fonte.contribuir(dadosBase.copy(nome = "   ")).dezenas.isEmpty())
    }

    @Test
    fun letrasViramDezenasPelaPosicaoNoAlfabeto() {
        // A=1, N=14, A(repete)=1(descartado). "ANA" -> [1, 14].
        assertEquals(listOf(1, 14), dezenasDoNome("ANA"))
    }

    @Test
    fun letraZViraDezena1PorNaoCabersEmUmaSoDezena() {
        // Z = 26, acima de 25: volta pro início (26 - 25 = 1).
        assertEquals(listOf(1), dezenasDoNome("Z"))
    }

    @Test
    fun caracteresNaoAlfabeticosENaoAsciiSaoIgnorados() {
        // "Álvaro" maiúsculo é "ÁLVARO": Á não está em A..Z (não é
        // normalizado pra A), então é descartado — sobra L,V,A,R,O.
        assertEquals(listOf(12, 22, 1, 18, 15), dezenasDoNome("Álvaro"))
    }

    @Test
    fun paraNoOitavoValorUnicoEncontrado() {
        // "ABCDEFGHIJ" -> A..H = 1..8 (8 valores únicos); I e J não entram.
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), dezenasDoNome("ABCDEFGHIJ"))
    }
}
