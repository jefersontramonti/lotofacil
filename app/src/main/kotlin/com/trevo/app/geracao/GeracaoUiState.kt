package com.trevo.app.geracao

// RF-02.9: "no mínimo três frases sequenciais" — o wireframe 1f usa quatro,
// e os índices de 0 a QUANTIDADE_DE_FRASES-1 têm que bater com os `when` de
// TelaGerando.kt.
const val QUANTIDADE_DE_FRASES_DO_RITUAL = 4

data class GeracaoUiState(
    val indiceFrase: Int = 0,
    val concluido: Boolean = false,
)
