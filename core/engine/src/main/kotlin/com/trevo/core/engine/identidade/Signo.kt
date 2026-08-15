package com.trevo.core.engine.identidade

import java.time.LocalDate
import java.time.MonthDay

enum class Signo(
    val inicio: MonthDay,
    val dezenas: List<Int>,
) {
    CAPRICORNIO(MonthDay.of(12, 22), listOf(4, 8, 13, 17, 22)),
    AQUARIO(MonthDay.of(1, 20), listOf(2, 11, 15, 20, 24)),
    PEIXES(MonthDay.of(2, 19), listOf(3, 7, 12, 16, 25)),
    ARIES(MonthDay.of(3, 21), listOf(1, 9, 14, 18, 23)),
    TOURO(MonthDay.of(4, 21), listOf(6, 10, 15, 19, 24)),
    GEMEOS(MonthDay.of(5, 21), listOf(5, 11, 13, 21, 25)),
    CANCER(MonthDay.of(6, 21), listOf(2, 7, 12, 20, 22)),
    LEAO(MonthDay.of(7, 23), listOf(1, 5, 10, 19, 23)),
    VIRGEM(MonthDay.of(8, 23), listOf(3, 8, 14, 17, 24)),
    LIBRA(MonthDay.of(9, 23), listOf(6, 9, 13, 18, 21)),
    ESCORPIAO(MonthDay.of(10, 23), listOf(4, 8, 16, 20, 25)),
    SAGITARIO(MonthDay.of(11, 22), listOf(2, 6, 11, 15, 22)),
}

// Busca o último signo cujo início é <= a data informada. Capricórnio
// atravessa a virada do ano (22/12-19/01): qualquer data anterior ao início
// de Aquário (20/01, o menor início do calendário) é, por regra de domínio,
// Capricórnio — não um caminho de fallback para entrada não reconhecida.
fun signoDe(nascimento: LocalDate): Signo {
    val diaEMes = MonthDay.from(nascimento)
    return if (diaEMes < Signo.AQUARIO.inicio) {
        Signo.CAPRICORNIO
    } else {
        Signo.entries.filter { it.inicio <= diaEMes }.maxBy { it.inicio }
    }
}
