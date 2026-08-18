package com.trevo.app.detalhe

import java.math.BigDecimal

data class DesdobramentosUiState(
    val carregando: Boolean = true,
    val quantidadeDeDezenas: Int = 0,
    val jogosEquivalentes: Long = 0,
    val custoTotal: BigDecimal = BigDecimal.ZERO,
    val combinacoesExibidas: List<List<Int>> = emptyList(),
)
