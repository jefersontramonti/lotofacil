package com.trevo.core.data.palpite

import com.trevo.core.engine.palpite.Palpite
import java.time.Instant

data class PalpiteSalvo(
    val id: Long,
    val palpite: Palpite,
    val criadoEm: Instant,
)
