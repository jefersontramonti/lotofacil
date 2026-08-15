package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FaseDaLuaTest {
    @Test
    fun dataDaLuaNovaDeReferenciaCaiEmNova() {
        assertEquals(FaseDaLua.NOVA, faseDaLuaEm(LocalDate.of(2000, 1, 6)))
    }

    @Test
    fun mesmaDataSempreProduzAMesmaFase() {
        val data = LocalDate.of(2026, 8, 14)
        assertEquals(faseDaLuaEm(data), faseDaLuaEm(data))
    }

    @Test
    fun dataAntesDaReferenciaNaoLancaExcecao() {
        // Regressão: o resto de divisão de dias negativos precisa ser
        // normalizado pra um índice válido de fase, nunca negativo.
        val fase = faseDaLuaEm(LocalDate.of(1978, 7, 14))
        assertTrue(fase in FaseDaLua.entries)
    }

    @Test
    fun cadaFaseTemAoMenosUmaDezenaEAsOitoFasesCobremAs25DezenasSemSobreposicao() {
        val dezenasPorFase = FaseDaLua.entries.associateWith { dezenasDaFaseDaLua(it) }

        dezenasPorFase.values.forEach { dezenas -> assertTrue(dezenas.isNotEmpty()) }

        val todasAsDezenas = dezenasPorFase.values.flatten()
        assertEquals(25, todasAsDezenas.size)
        assertEquals((1..25).toList(), todasAsDezenas.sorted())
    }
}
