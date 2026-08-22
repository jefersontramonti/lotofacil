package com.trevo.app.conferencia

import com.trevo.core.engine.resultado.FaixaDePremio
import java.math.BigDecimal
import java.time.LocalDate

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
        val dataApuracao: LocalDate,
        val dezenasSorteadas: List<Int>,
        val totalGanho: BigDecimal,
        val totalGasto: BigDecimal,
        val itens: List<PalpiteConferidoUiState>,
        val origemManual: Boolean,
        // RF-05.10: faixasDePremio vem vazia pra resultado manual — a
        // tabela de premiação oficial só existe na resposta da Caixa.
        val faixasDePremio: List<FaixaDePremio>,
        val acumulado: Boolean,
    ) : ConferenciaUiState
}

data class PalpiteConferidoUiState(
    val numeroDoDia: Int,
    val dezenas: List<Int>,
    val dezenasAcertadas: Set<Int>,
    val acertos: Int,
    val premio: BigDecimal?,
)
