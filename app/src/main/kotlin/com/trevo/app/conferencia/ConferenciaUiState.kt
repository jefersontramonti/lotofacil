package com.trevo.app.conferencia

import java.math.BigDecimal

sealed interface ConferenciaUiState {
    data object Carregando : ConferenciaUiState

    // RF-05.7 — o resultado mais recente salvo é anterior à data de criação
    // dos palpites de hoje: o sorteio relevante ainda não saiu.
    data object Espera : ConferenciaUiState

    // RF-05.8
    data object SemConexao : ConferenciaUiState

    // RF-05.9
    data object Falha : ConferenciaUiState

    data class Sucesso(
        val numeroDoConcurso: Int?,
        val dezenasSorteadas: List<Int>,
        val totalGanho: BigDecimal,
        val totalGasto: BigDecimal,
        val itens: List<PalpiteConferidoUiState>,
        val origemManual: Boolean,
    ) : ConferenciaUiState
}

data class PalpiteConferidoUiState(
    val numeroDoDia: Int,
    val dezenas: List<Int>,
    val dezenasAcertadas: Set<Int>,
    val acertos: Int,
    val premio: BigDecimal?,
)
