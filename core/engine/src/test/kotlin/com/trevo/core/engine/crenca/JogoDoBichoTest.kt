package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class JogoDoBichoTest {
    @Test
    fun grupo1PuxaADezena1EAEspelhada25() {
        assertEquals(listOf(1, 25), dezenasDoGrupoDoBicho(1))
    }

    @Test
    fun grupo12PuxaADezena12EAEspelhada14() {
        assertEquals(listOf(12, 14), dezenasDoGrupoDoBicho(12))
    }

    @Test
    fun grupo13TemDezenaEEspelhadaIguaisEDevolveUmaUnicaDezena() {
        // 26 - 13 = 13: a dezena aparece uma vez só, não duas.
        assertEquals(listOf(13), dezenasDoGrupoDoBicho(13))
    }

    @Test
    fun grupo25PuxaADezena25EAEspelhada1() {
        assertEquals(listOf(1, 25), dezenasDoGrupoDoBicho(25))
    }

    @Test
    fun grupoForaDoIntervaloLancaExcecao() {
        assertThrows(IllegalArgumentException::class.java) { dezenasDoGrupoDoBicho(0) }
        assertThrows(IllegalArgumentException::class.java) { dezenasDoGrupoDoBicho(26) }
    }
}
