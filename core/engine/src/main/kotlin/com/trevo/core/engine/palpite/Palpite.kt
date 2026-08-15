package com.trevo.core.engine.palpite

import com.trevo.core.engine.crenca.Crenca

data class Palpite(
    val dezenas: List<Int>,
    val dezenasFixas: List<Int>,
    val contribuicoes: Map<Crenca, List<Int>>,
    val forca: Int,
)
