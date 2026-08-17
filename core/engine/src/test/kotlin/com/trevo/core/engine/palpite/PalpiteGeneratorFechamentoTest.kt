package com.trevo.core.engine.palpite

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

// RF-02.8 — "Gerar fechamentos de 16, 18 e 20 dezenas pelo mesmo motor."
class PalpiteGeneratorFechamentoTest {
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun cadaTamanhoDeFechamentoGeraAQuantidadeCorrespondenteDeDezenasSemRepeticao() {
        TamanhoDeFechamento.entries.forEach { tamanho ->
            val fechamento =
                PalpiteGenerator(Random(1)).gerarFechamento(
                    tamanho = tamanho,
                    crencasAtivas = emptySet(),
                    dados = dadosBase,
                )

            assertEquals(tamanho.quantidade, fechamento.dezenas.size)
            assertEquals(fechamento.dezenas.size, fechamento.dezenas.toSet().size)
            assertTrue(fechamento.dezenas.all { it in 1..25 })
        }
    }

    @Test
    fun gerarFechamentoUsaOMesmoMotorDeGerarMesmaSementeMesmasCrencasMesmoResultado() {
        val semente = 7
        val crencas = setOf(Crenca.MOLDURA, Crenca.PARES)

        val viaFechamento =
            PalpiteGenerator(Random(semente)).gerarFechamento(
                tamanho = TamanhoDeFechamento.VINTE,
                crencasAtivas = crencas,
                dados = dadosBase,
            )
        val viaGerar =
            PalpiteGenerator(Random(semente)).gerar(
                crencasAtivas = crencas,
                dados = dadosBase,
                quantidade = TamanhoDeFechamento.VINTE.quantidade,
            )

        assertEquals(viaGerar, viaFechamento)
    }

    @Test
    fun dezenasFixasEntramSempreNoFechamento() {
        val fixas = setOf(1, 2, 3)

        val fechamento =
            PalpiteGenerator(Random(3)).gerarFechamento(
                tamanho = TamanhoDeFechamento.DEZOITO,
                crencasAtivas = emptySet(),
                dados = dadosBase,
                dezenasFixas = fixas,
            )

        assertTrue(fixas.all { it in fechamento.dezenas })
    }

    @Test
    fun osTresTamanhosDeFechamentoSaoExatamente16_18e20() {
        assertEquals(
            listOf(16, 18, 20),
            TamanhoDeFechamento.entries.map { it.quantidade },
        )
    }
}
