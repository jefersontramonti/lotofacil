package com.trevo.app.onboarding

import com.trevo.core.engine.crenca.Crenca

data class CrencasUiState(
    val selecionadas: Set<Crenca> = emptySet(),
)
