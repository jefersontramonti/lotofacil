package com.trevo.core.engine.palpite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CombinatoriaTest {
    @Test
    fun coeficienteBinomialBateComATabelaOficialDeFechamentos() {
        // Docs/tabelavalores.md — jogos de 15 equivalentes por fechamento.
        assertEquals(1L, coeficienteBinomial(15, 15))
        assertEquals(16L, coeficienteBinomial(16, 15))
        assertEquals(136L, coeficienteBinomial(17, 15))
        assertEquals(816L, coeficienteBinomial(18, 15))
        assertEquals(3876L, coeficienteBinomial(19, 15))
        assertEquals(15504L, coeficienteBinomial(20, 15))
    }

    @Test
    fun coeficienteBinomialDosCasosDeBorda() {
        assertEquals(1L, coeficienteBinomial(25, 0))
        assertEquals(1L, coeficienteBinomial(25, 25))
    }

    @Test
    fun coeficienteBinomialComKForaDoIntervaloLancaExcecao() {
        assertThrows(IllegalArgumentException::class.java) { coeficienteBinomial(10, 11) }
        assertThrows(IllegalArgumentException::class.java) { coeficienteBinomial(10, -1) }
    }

    @Test
    fun probabilidadeDe15AcertosParaUmPalpiteDe15EUmEm3268760() {
        val probabilidade = probabilidadeDe15Acertos(15)

        assertEquals(1L, probabilidade.numerador)
        assertEquals(3_268_760L, probabilidade.denominador)
    }

    @Test
    fun probabilidadeDe15AcertosCresceComOTamanhoDoFechamento() {
        val probabilidade18 = probabilidadeDe15Acertos(18)

        assertEquals(816L, probabilidade18.numerador)
        assertEquals(3_268_760L, probabilidade18.denominador)
    }

    @Test
    fun probabilidadeForaDoIntervaloValidoLancaExcecao() {
        assertThrows(IllegalArgumentException::class.java) { probabilidadeDe15Acertos(14) }
        assertThrows(IllegalArgumentException::class.java) { probabilidadeDe15Acertos(26) }
    }

    @Test
    fun combinacoesDe15DeExatamente15DezenasDevolveUmaUnicaCombinacaoIgualAsDezenas() {
        val dezenas = (1..15).toList()

        val combinacoes = combinacoesDe15(dezenas).toList()

        assertEquals(1, combinacoes.size)
        assertEquals(dezenas, combinacoes.first())
    }

    @Test
    fun combinacoesDe15DeUmFechamentoDe18GeraExatamente816CombinacoesDistintasDe15Unicas() {
        val dezenas = (1..18).toList()

        val combinacoes = combinacoesDe15(dezenas).toList()

        assertEquals(816, combinacoes.size)
        assertEquals(816, combinacoes.toSet().size)
        combinacoes.forEach { combinacao ->
            assertEquals(15, combinacao.size)
            assertEquals(15, combinacao.toSet().size)
            assertEquals(combinacao.sorted(), combinacao)
            assertTrue(combinacao.all { it in dezenas })
        }
    }

    @Test
    fun combinacoesDe15ConsegueParcialSemMaterializarTudoAteEncontrarUmFechamentoDe20() {
        val dezenas = (1..20).toList()

        val primeiras24 = combinacoesDe15(dezenas).take(24).toList()

        assertEquals(24, primeiras24.size)
        assertEquals(24, primeiras24.toSet().size)
    }

    @Test
    fun combinacoesDe15ComMenosDe15DezenasLancaExcecao() {
        assertThrows(IllegalArgumentException::class.java) { combinacoesDe15((1..14).toList()).toList() }
    }
}
