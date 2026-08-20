package com.trevo.core.engine.notificacao

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class ProximoDisparoTest {
    @Test
    fun `horario ainda nao chegou hoje agenda para hoje mesmo`() {
        val agora = LocalDateTime.of(2026, 8, 19, 10, 0)
        val atraso = calcularAtrasoAteProximoHorario(agora, LocalTime.of(18, 0))
        assertEquals(Duration.ofHours(8), atraso)
    }

    @Test
    fun `horario ja passou hoje agenda para amanha`() {
        val agora = LocalDateTime.of(2026, 8, 19, 19, 30)
        val atraso = calcularAtrasoAteProximoHorario(agora, LocalTime.of(18, 0))
        assertEquals(Duration.ofHours(22).plusMinutes(30), atraso)
    }

    @Test
    fun `horario e exatamente agora agenda para amanha, nunca dispara no mesmo instante`() {
        val agora = LocalDateTime.of(2026, 8, 19, 18, 0)
        val atraso = calcularAtrasoAteProximoHorario(agora, LocalTime.of(18, 0))
        assertEquals(Duration.ofDays(1), atraso)
    }

    @Test
    fun `atravessa virada de mes corretamente`() {
        val agora = LocalDateTime.of(2026, 8, 31, 20, 0)
        val atraso = calcularAtrasoAteProximoHorario(agora, LocalTime.of(18, 0))
        assertEquals(Duration.ofHours(22), atraso)
        assertEquals(LocalDateTime.of(2026, 9, 1, 18, 0), agora.plus(atraso))
    }
}
