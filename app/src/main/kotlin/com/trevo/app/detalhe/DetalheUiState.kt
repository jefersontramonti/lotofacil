package com.trevo.app.detalhe

import com.trevo.core.engine.crenca.Crenca
import java.math.BigDecimal

data class OrigemDeDezenasUiState(
    val crenca: Crenca,
    val dezenas: List<Int>,
)

data class DetalheUiState(
    val carregando: Boolean = true,
    val palpiteExiste: Boolean = true,
    val numeroDoDia: Int = 0,
    val dezenas: List<Int> = emptyList(),
    val dezenasFixas: List<Int> = emptyList(),
    val forca: Int = 0,
    val origens: List<OrigemDeDezenasUiState> = emptyList(),
    val soma: Int = 0,
    val pares: Int = 0,
    val impares: Int = 0,
    val moldura: Int = 0,
    val miolo: Int = 0,
    val custo: BigDecimal = BigDecimal.ZERO,
    // RF-04.4 — probabilidade real de 15 acertos, como "1 em N".
    val chanceRealUmEm: Long = 0,
    val quantidadeDeDezenas: Int = 15,
    val podeVerDesdobramentos: Boolean = false,
    val palpiteParaConfirmarExclusao: Boolean = false,
    val modoEdicao: Boolean = false,
    val dezenasEmEdicao: Set<Int> = emptySet(),
    val guardarComoFixasAoSalvar: Boolean = false,
) {
    val faltamOuSobram: Int get() = quantidadeDeDezenas - dezenasEmEdicao.size
}
