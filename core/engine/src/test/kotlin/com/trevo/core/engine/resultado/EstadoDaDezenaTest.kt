package com.trevo.core.engine.resultado

import com.trevo.core.engine.palpite.Palpite
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class EstadoDaDezenaTest {
    private val palpite =
        Palpite(
            dezenas = listOf(1, 2, 3, 4, 5),
            dezenasFixas = emptyList(),
            contribuicoes = emptyMap(),
            forca = 100,
        )

    private val resultado =
        Resultado(
            numero = 1,
            dataApuracao = LocalDate.of(2026, 1, 1),
            dezenasSorteadas = listOf(1, 2, 6, 7),
            faixasDePremio = emptyList(),
            acumulado = false,
            origem = OrigemDoResultado.API,
            proximoConcurso = null,
        )

    @Test
    fun cobreAsVinteECincoDezenas() {
        val estados = estadosDasDezenas(palpite, resultado)

        assertEquals((1..25).toSet(), estados.keys)
    }

    @Test
    fun marcadaESorteadaEAcertada() {
        val estados = estadosDasDezenas(palpite, resultado)

        assertEquals(EstadoDaDezena.ACERTADA, estados.getValue(1))
        assertEquals(EstadoDaDezena.ACERTADA, estados.getValue(2))
    }

    @Test
    fun marcadaMasNaoSorteadaFicaMarcadaNaoSaiu() {
        val estados = estadosDasDezenas(palpite, resultado)

        assertEquals(EstadoDaDezena.MARCADA_NAO_SAIU, estados.getValue(3))
        assertEquals(EstadoDaDezena.MARCADA_NAO_SAIU, estados.getValue(4))
        assertEquals(EstadoDaDezena.MARCADA_NAO_SAIU, estados.getValue(5))
    }

    @Test
    fun sorteadaMasNaoMarcadaFicaSorteadaNaoMarcada() {
        val estados = estadosDasDezenas(palpite, resultado)

        assertEquals(EstadoDaDezena.SORTEADA_NAO_MARCADA, estados.getValue(6))
        assertEquals(EstadoDaDezena.SORTEADA_NAO_MARCADA, estados.getValue(7))
    }

    @Test
    fun nemMarcadaNemSorteadaFicaNeutra() {
        val estados = estadosDasDezenas(palpite, resultado)

        assertEquals(EstadoDaDezena.NEUTRA, estados.getValue(8))
        assertEquals(EstadoDaDezena.NEUTRA, estados.getValue(25))
    }
}
