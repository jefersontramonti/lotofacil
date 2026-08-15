package com.trevo.core.engine.crenca

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class FaseDaLua {
    NOVA,
    CRESCENTE_INICIAL,
    QUARTO_CRESCENTE,
    CRESCENTE_GIBOSA,
    CHEIA,
    MINGUANTE_GIBOSA,
    QUARTO_MINGUANTE,
    MINGUANTE_FINAL,
}

private const val CICLO_LUNAR_EM_DIAS = 29.530588853
private val LUA_NOVA_DE_REFERENCIA: LocalDate = LocalDate.of(2000, 1, 6)

fun faseDaLuaEm(data: LocalDate): FaseDaLua {
    val diasDesdeReferencia = ChronoUnit.DAYS.between(LUA_NOVA_DE_REFERENCIA, data).toDouble()
    val restoDaDivisao = diasDesdeReferencia % CICLO_LUNAR_EM_DIAS
    val posicaoNoCiclo = if (restoDaDivisao < 0) restoDaDivisao + CICLO_LUNAR_EM_DIAS else restoDaDivisao
    val indice = (posicaoNoCiclo / CICLO_LUNAR_EM_DIAS * 8).toInt().coerceIn(0, 7)
    return FaseDaLua.entries[indice]
}

// Divisão popular das 25 dezenas pelas 8 fases — sem fundamento
// astronômico, mesma natureza declarada do jogo do bicho (tradição, não
// previsão; regra inviolável 2, CLAUDE.md §1).
fun dezenasDaFaseDaLua(fase: FaseDaLua): List<Int> {
    val indice = fase.ordinal
    return (1..25).filter { (it - 1) % 8 == indice }
}
