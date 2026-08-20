package com.trevo.core.engine.notificacao

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

// RF-07.3/RF-07.6 — usada tanto para o primeiro agendamento quanto para o
// reagendamento diário (worker chama de novo, com `agora` = instante em que
// acabou de disparar). Se o horário-alvo de hoje já passou — ou é agora
// mesmo — agenda para amanhã: nunca devolve um atraso de zero/negativo, que
// faria o WorkManager disparar de novo na hora.
fun calcularAtrasoAteProximoHorario(
    agora: LocalDateTime,
    horarioAlvo: LocalTime,
): Duration {
    val hojeNoHorarioAlvo = agora.toLocalDate().atTime(horarioAlvo)
    val proximaOcorrencia = if (hojeNoHorarioAlvo.isAfter(agora)) hojeNoHorarioAlvo else hojeNoHorarioAlvo.plusDays(1)
    return Duration.between(agora, proximaOcorrencia)
}
