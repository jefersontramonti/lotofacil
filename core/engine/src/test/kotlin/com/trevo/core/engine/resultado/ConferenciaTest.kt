package com.trevo.core.engine.resultado

import com.trevo.core.engine.palpite.Palpite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ConferenciaTest {
    private val palpiteDeExemplo =
        Palpite(
            dezenas = (1..15).toList(),
            dezenasFixas = emptyList(),
            contribuicoes = emptyMap(),
            forca = 80,
        )

    private fun resultadoComDezenas(
        dezenasSorteadas: List<Int>,
        faixas: List<FaixaDePremio> =
            listOf(
                FaixaDePremio(15, 0, BigDecimal.ZERO),
                FaixaDePremio(14, 216, BigDecimal("2399.70")),
                FaixaDePremio(13, 6627, BigDecimal("35.00")),
                FaixaDePremio(12, 81833, BigDecimal("14.00")),
                FaixaDePremio(11, 448567, BigDecimal("7.00")),
            ),
    ) = Resultado(
        numero = 3457,
        dataApuracao = LocalDate.of(2025, 7, 31),
        dezenasSorteadas = dezenasSorteadas,
        faixasDePremio = faixas,
        acumulado = true,
        origem = OrigemDoResultado.API,
        proximoConcurso = null,
    )

    @Test
    fun quinzeAcertosDevolveAFaixaDe15() {
        val resultado = resultadoComDezenas((1..15).toList())

        val conferencia = conferir(palpiteDeExemplo, resultado)

        assertEquals(15, conferencia.acertos)
        assertEquals(15, conferencia.faixa?.acertosNecessarios)
    }

    @Test
    fun trezeAcertosDevolveAFaixaDe13() {
        // 13 das 15 dezenas do palpite batem: sorteio troca 14 e 15 por 16 e 17.
        val resultado = resultadoComDezenas((1..13).toList() + listOf(16, 17))

        val conferencia = conferir(palpiteDeExemplo, resultado)

        assertEquals(13, conferencia.acertos)
        assertEquals(13, conferencia.faixa?.acertosNecessarios)
        assertEquals(BigDecimal("35.00"), conferencia.faixa?.valorPremio)
    }

    @Test
    fun menosDeOnzeAcertosNaoTemFaixaPremiada() {
        val resultado = resultadoComDezenas((11..25).toList())

        val conferencia = conferir(palpiteDeExemplo, resultado)

        assertEquals(5, conferencia.acertos)
        assertNull(conferencia.faixa)
    }

    @Test
    fun resultadoManualSemFaixasNuncaAtribuiPremio() {
        val resultado =
            resultadoComDezenas(
                (1..15).toList(),
                faixas = emptyList(),
            ).copy(origem = OrigemDoResultado.MANUAL)

        val conferencia = conferir(palpiteDeExemplo, resultado)

        assertEquals(15, conferencia.acertos)
        assertNull(conferencia.faixa)
    }
}
