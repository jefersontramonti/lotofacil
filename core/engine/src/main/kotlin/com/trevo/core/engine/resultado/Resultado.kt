package com.trevo.core.engine.resultado

import java.math.BigDecimal
import java.time.LocalDate

// RF-05.10: um resultado inserido manualmente (API indisponível por muito
// tempo) não tem faixasDePremio — o valor do prêmio só existe na resposta
// oficial da Caixa, nunca é inventado (CLAUDE.md §8).
enum class OrigemDoResultado {
    API,
    MANUAL,
}

data class FaixaDePremio(
    val acertosNecessarios: Int,
    val numeroDeGanhadores: Long,
    val valorPremio: BigDecimal,
)

// `numero` é nulo pra um resultado inserido manualmente (RF-05.10) — o
// app nunca calcula/inventa número de concurso offline.
data class Resultado(
    val numero: Int?,
    val dataApuracao: LocalDate,
    val dezenasSorteadas: List<Int>,
    val faixasDePremio: List<FaixaDePremio>,
    val acumulado: Boolean,
    val origem: OrigemDoResultado,
)
