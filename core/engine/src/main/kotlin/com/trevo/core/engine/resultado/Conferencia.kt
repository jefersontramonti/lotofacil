package com.trevo.core.engine.resultado

import com.trevo.core.engine.palpite.Palpite

data class Conferencia(
    val acertos: Int,
    val faixa: FaixaDePremio?,
)

// RF-05.4 — quantidade de acertos e a faixa premiada (se houver) de um
// palpite contra um resultado. Pura: não lança, não acessa rede/banco.
fun conferir(
    palpite: Palpite,
    resultado: Resultado,
): Conferencia {
    val sorteadas = resultado.dezenasSorteadas.toSet()
    val acertos = palpite.dezenas.count { it in sorteadas }
    val faixa = resultado.faixasDePremio.firstOrNull { it.acertosNecessarios == acertos }
    return Conferencia(acertos = acertos, faixa = faixa)
}
