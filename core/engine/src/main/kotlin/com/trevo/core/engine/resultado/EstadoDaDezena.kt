package com.trevo.core.engine.resultado

import com.trevo.core.engine.palpite.Palpite

// RF-05.5 — as três marcações exigidas no volante completo (25 dezenas):
// acerto, marcada que não saiu, e sorteada que não foi marcada.
enum class EstadoDaDezena {
    ACERTADA,
    MARCADA_NAO_SAIU,
    SORTEADA_NAO_MARCADA,
    NEUTRA,
}

fun estadosDasDezenas(
    palpite: Palpite,
    resultado: Resultado,
): Map<Int, EstadoDaDezena> {
    val marcadas = palpite.dezenas.toSet()
    val sorteadas = resultado.dezenasSorteadas.toSet()
    return (1..25).associateWith { dezena ->
        when {
            dezena in marcadas && dezena in sorteadas -> EstadoDaDezena.ACERTADA
            dezena in marcadas -> EstadoDaDezena.MARCADA_NAO_SAIU
            dezena in sorteadas -> EstadoDaDezena.SORTEADA_NAO_MARCADA
            else -> EstadoDaDezena.NEUTRA
        }
    }
}
