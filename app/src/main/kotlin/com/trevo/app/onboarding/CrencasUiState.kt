package com.trevo.app.onboarding

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite

data class CrencasUiState(
    val selecionadas: Set<Crenca> = emptySet(),
    val palpiteGerado: Palpite? = null,
)
