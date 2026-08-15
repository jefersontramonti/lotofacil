package com.trevo.app.onboarding

import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo

// nascimentoValido significa só "data bem formada" (RF-01.3) — não muda com
// RF-01.4. Para um menor de idade o par fica nascimentoValido = true e
// erroNascimento = MENOR_DE_IDADE, porque a data continua calculável — é o
// que RF-01.5 usa para o signo, independente da checagem de idade.
// `signo` é `null` sempre que a data não é validamente formada (marcador
// neutro), calculado a partir do mesmo LocalDate validado, nunca reparseado.
data class IdentidadeUiState(
    val nome: String = "",
    val nascimento: String = "",
    val nascimentoValido: Boolean = false,
    val erroNascimento: ErroDataNascimento? = null,
    val signo: Signo? = null,
)
