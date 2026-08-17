package com.trevo.app.home

import java.math.BigDecimal

val CUSTO_POR_JOGO: BigDecimal = BigDecimal("3.00")

data class PalpiteItemUiState(
    val id: Long,
    val numeroDoDia: Int,
    val dezenas: List<Int>,
    val forca: Int,
    val horario: String,
)

data class HomeUiState(
    val carregando: Boolean = true,
    val palpitesHoje: List<PalpiteItemUiState> = emptyList(),
    val palpiteParaConfirmarExclusao: Long? = null,
) {
    val totalDeJogos: Int get() = palpitesHoje.size

    val custoTotal: BigDecimal get() = CUSTO_POR_JOGO.multiply(BigDecimal(totalDeJogos))
}
