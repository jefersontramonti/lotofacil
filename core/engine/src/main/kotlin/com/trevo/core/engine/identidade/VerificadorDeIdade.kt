package com.trevo.core.engine.identidade

import java.time.Clock
import java.time.LocalDate
import java.time.Period

const val IDADE_MINIMA_PARA_CADASTRO = 18

/**
 * `Period.between(nascimento, hoje).years`, nunca subtração de `.year` — o
 * bug do protótipo (`Docs/Trevo - Lotofácil.dc.html`, `validaNasc`) conta
 * como maior de idade quem ainda não fez aniversário no ano corrente.
 *
 * Quem nasce em 29/02 completa 18 anos, em ano não bissexto, em 01/03 — não
 * em 28/02. É o comportamento de `java.time.Period` (verificado, não
 * deduzido) e também a opção mais restritiva das duas possíveis: usar
 * `nascimento.plusYears(18)` faz clamping para 28/02 e liberaria o cadastro
 * um dia antes. Diante da regra inviolável 5 (CLAUDE.md §1), a mais
 * restritiva vence. Para essa pessoa não existe um dia com "18a 0m 0d" — o
 * período salta de "17a 11m 30d" (28/02) para "18a 0m 1d" (01/03) — por isso
 * a comparação é sempre sobre `.years`, nunca sobre igualdade de período.
 */
fun idadeEmAnosCompletos(
    nascimento: LocalDate,
    hoje: LocalDate,
): Int = Period.between(nascimento, hoje).years

class VerificadorDeIdade(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun ehMaiorDeIdade(nascimento: LocalDate): Boolean {
        val hoje = LocalDate.now(clock)
        return idadeEmAnosCompletos(nascimento, hoje) >= IDADE_MINIMA_PARA_CADASTRO
    }
}
