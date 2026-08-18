package com.trevo.core.data.preferencias

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import java.time.LocalDate

data class PerfilSalvo(
    val nome: String,
    val nascimento: LocalDate?,
    val signo: Signo?,
    val crencasAtivas: Set<Crenca>,
)
