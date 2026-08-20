package com.trevo.core.engine.palpite

import com.trevo.core.engine.crenca.Amuleto
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.OpcaoDeAmuleto
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class PalpiteGeneratorRitualTest {
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun sorteiaUmaDezenaEntre1E25() {
        val dezena =
            PalpiteGenerator(Random(1)).sortearDezenaDoRitual(
                crencasAtivas = emptySet(),
                dados = dadosBase,
                dezenasExcluidas = emptySet(),
            )

        assertTrue(dezena in 1..25)
    }

    @Test
    fun nuncaSorteiaUmaDezenaJaExcluida() {
        val excluidas = (1..24).toSet()

        repeat(20) { semente ->
            val dezena =
                PalpiteGenerator(Random(semente)).sortearDezenaDoRitual(
                    crencasAtivas = emptySet(),
                    dados = dadosBase,
                    dezenasExcluidas = excluidas,
                )
            assertEquals(25, dezena)
        }
    }

    @Test
    fun tresSorteiosSucessivosExcluindoOsAnterioresNuncaRepetem() {
        val gerador = PalpiteGenerator(Random(7))
        val d1 = gerador.sortearDezenaDoRitual(emptySet(), dadosBase, emptySet())
        val d2 = gerador.sortearDezenaDoRitual(emptySet(), dadosBase, setOf(d1))
        val d3 = gerador.sortearDezenaDoRitual(emptySet(), dadosBase, setOf(d1, d2))

        assertEquals(3, setOf(d1, d2, d3).size)
    }

    @Test
    fun mesmaSementeProduzOMesmoSorteioDeRitual() {
        val dezena1 =
            PalpiteGenerator(Random(42)).sortearDezenaDoRitual(setOf(Crenca.MOLDURA), dadosBase, emptySet())
        val dezena2 =
            PalpiteGenerator(Random(42)).sortearDezenaDoRitual(setOf(Crenca.MOLDURA), dadosBase, emptySet())

        assertEquals(dezena1, dezena2)
    }

    @Test
    fun gerarRegistraModoERitualNoPalpite() {
        val ritual =
            listOf(
                RevelacaoDoAmuleto(Amuleto.TREVO, OpcaoDeAmuleto.TREVO_SORTE, 17),
                RevelacaoDoAmuleto(Amuleto.FERRADURA, OpcaoDeAmuleto.FERRADURA_MEIO, 8),
                RevelacaoDoAmuleto(Amuleto.ANEIS, OpcaoDeAmuleto.ANEIS_SEGUNDO, 23),
            )

        val palpite =
            PalpiteGenerator(Random(1)).gerar(
                crencasAtivas = emptySet(),
                dados = dadosBase,
                dezenasFixas = setOf(17, 8, 23),
                modo = ModoDeGeracao.DESTINO,
                ritual = ritual,
            )

        assertEquals(ModoDeGeracao.DESTINO, palpite.modo)
        assertEquals(ritual, palpite.ritual)
        assertTrue(setOf(17, 8, 23).all { it in palpite.dezenas })
    }

    @Test
    fun palpiteSemModoNemRitualUsaOsPadroesNeutros() {
        val palpite = PalpiteGenerator(Random(1)).gerar(crencasAtivas = emptySet(), dados = dadosBase)

        assertEquals(null, palpite.modo)
        assertTrue(palpite.ritual.isEmpty())
        assertFalse(palpite.dezenas.isEmpty())
    }
}
