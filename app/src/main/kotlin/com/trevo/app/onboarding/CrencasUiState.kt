package com.trevo.app.onboarding

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite

const val LIMITE_DE_CRENCAS_NO_GRATIS = 3

data class CrencasUiState(
    val selecionadas: Set<Crenca> = emptySet(),
    val isPro: Boolean = false,
    val palpiteGerado: Palpite? = null,
) {
    // RF-01.8: no plano grátis, trava a seleção após 3 crenças ativas.
    // Uma crença já selecionada nunca fica bloqueada por essa regra —
    // senão desmarcá-la seria impossível depois de atingir o limite.
    fun crencaBloqueada(crenca: Crenca): Boolean =
        !isPro && crenca !in selecionadas && selecionadas.size >= LIMITE_DE_CRENCAS_NO_GRATIS
}
