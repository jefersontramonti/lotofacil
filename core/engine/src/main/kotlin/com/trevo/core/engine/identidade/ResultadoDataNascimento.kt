package com.trevo.core.engine.identidade

import java.time.LocalDate

enum class ErroDataNascimento {
    VAZIO,
    FORMATO_INVALIDO,
    MES_INVALIDO,
    DIA_INEXISTENTE,
    FORA_DO_INTERVALO,

    // Produzido só por IdentidadeViewModel a partir de VerificadorDeIdade
    // (RF-01.4) — ValidadorDataNascimento.validar nunca devolve este valor.
    // São dois julgamentos distintos sobre o mesmo LocalDate: uma data pode
    // ser formalmente válida e ainda assim reprovar por idade.
    MENOR_DE_IDADE,
}

sealed interface ResultadoDataNascimento {
    data class Valida(
        val data: LocalDate,
    ) : ResultadoDataNascimento

    data class Invalida(
        val erro: ErroDataNascimento,
    ) : ResultadoDataNascimento
}
