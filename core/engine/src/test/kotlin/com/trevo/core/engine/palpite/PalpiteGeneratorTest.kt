package com.trevo.core.engine.palpite

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class PalpiteGeneratorTest {
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun gera15DezenasEntre1E25SemRepeticaoPorPadrao() {
        val palpite = PalpiteGenerator(Random(1)).gerar(crencasAtivas = emptySet(), dados = dadosBase)

        assertEquals(15, palpite.dezenas.size)
        assertEquals(palpite.dezenas.size, palpite.dezenas.toSet().size)
        assertTrue(palpite.dezenas.all { it in 1..25 })
        assertEquals(palpite.dezenas.sorted(), palpite.dezenas)
    }

    @Test
    fun mesmaSementeProduzASaidaExataDoPalpite() {
        // Saída capturada de uma execução real com Random(42) — CLAUDE.md
        // §4: todo teste do motor usa semente fixa e afirma saída exata.
        val palpite = PalpiteGenerator(Random(42)).gerar(crencasAtivas = emptySet(), dados = dadosBase)

        assertEquals(SAIDA_ESPERADA_SEMENTE_42, palpite.dezenas)
    }

    @Test
    fun sementesDiferentesTendemAProduzirSaidasDiferentes() {
        val palpite1 = PalpiteGenerator(Random(1)).gerar(crencasAtivas = emptySet(), dados = dadosBase)
        val palpite2 = PalpiteGenerator(Random(2)).gerar(crencasAtivas = emptySet(), dados = dadosBase)

        assertTrue(palpite1.dezenas != palpite2.dezenas)
    }

    @Test
    fun dezenasFixasEntramSempreNoResultadoFinal() {
        val fixas = setOf(1, 2, 3)

        repeat(20) { semente ->
            val palpite =
                PalpiteGenerator(
                    Random(semente),
                ).gerar(crencasAtivas = emptySet(), dados = dadosBase, dezenasFixas = fixas)
            assertTrue(fixas.all { it in palpite.dezenas })
        }
    }

    @Test
    fun quantidadeDiferenteDe15GeraOTamanhoPedido() {
        val palpite = PalpiteGenerator(Random(1)).gerar(crencasAtivas = emptySet(), dados = dadosBase, quantidade = 20)

        assertEquals(20, palpite.dezenas.size)
    }

    @Test
    fun quantidadeForaDoIntervaloLancaExcecao() {
        val gerador = PalpiteGenerator(Random(1))

        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            gerador.gerar(crencasAtivas = emptySet(), dados = dadosBase, quantidade = 26)
        }
    }

    @Test
    fun crencaSemDadoDeOrigemNaoQuebraAGeracaoEContribuiComListaVazia() {
        // RF-02.5: SIGNO ativa mas sem `signo` em `dados` — excluída do
        // cálculo sem exceção, aparece nas contribuições com lista vazia.
        val palpite =
            PalpiteGenerator(Random(1)).gerar(
                crencasAtivas = setOf(Crenca.SIGNO),
                dados = dadosBase.copy(signo = null),
            )

        assertEquals(15, palpite.dezenas.size)
        assertEquals(emptyList<Int>(), palpite.contribuicoes.getValue(Crenca.SIGNO))
    }

    @Test
    fun forcaEZeroQuandoNenhumaCrencaEstaAtiva() {
        val palpite = PalpiteGenerator(Random(1)).gerar(crencasAtivas = emptySet(), dados = dadosBase)

        assertEquals(0, palpite.forca)
    }

    @Test
    fun contribuicoesRegistramSoAsDezenasQueEntraramNoPalpiteFinal() {
        val palpite =
            PalpiteGenerator(Random(1)).gerar(
                crencasAtivas = setOf(Crenca.MOLDURA),
                dados = dadosBase,
            )

        val hitDaMoldura = palpite.contribuicoes.getValue(Crenca.MOLDURA)
        assertTrue(hitDaMoldura.all { it in palpite.dezenas })
    }

    companion object {
        // Preenchido depois de rodar o teste uma vez e observar a saída real.
        private val SAIDA_ESPERADA_SEMENTE_42 = listOf(2, 4, 6, 9, 11, 12, 13, 16, 17, 18, 21, 22, 23, 24, 25)
    }
}
