package com.trevo.app.historico

import java.math.BigDecimal
import java.time.LocalDate

sealed interface HistoricoUiState {
    data object Carregando : HistoricoUiState

    // RF-06.5
    data object Vazio : HistoricoUiState

    data class ComDados(
        val totalDeJogos: Int,
        val totalDeConcursos: Int,
        val totalGasto: BigDecimal,
        val totalGanho: BigDecimal,
        val saldo: BigDecimal,
        val retornoPercentual: Int,
        val mediaGastoPorConcurso: BigDecimal,
        val melhorResultadoEmAcertos: Int,
        // RF-06.3 — sempre as 5 faixas, 15 a 11, mesmo com contagem zero.
        val faixas: List<FaixaHistoricoUiState>,
        // RF-06.4 — só os concursos já revelados nesta página.
        val concursosRevelados: List<ConcursoConferidoUiState>,
        val temMaisConcursos: Boolean,
        val quantidadeDeConcursosRestantes: Int,
    ) : HistoricoUiState
}

data class FaixaHistoricoUiState(
    val acertos: Int,
    val quantidade: Int,
)

data class ConcursoConferidoUiState(
    val numero: Int?,
    val data: LocalDate,
    val premioTotal: BigDecimal,
    val palpites: List<PalpiteNoHistoricoUiState>,
)

data class PalpiteNoHistoricoUiState(
    val numeroDoDia: Int,
    val dezenas: List<Int>,
    val acertos: Int,
    val premio: BigDecimal?,
)
