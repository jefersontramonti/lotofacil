package com.trevo.core.engine.crenca

import com.trevo.core.engine.identidade.Signo
import java.time.LocalDate

// `historicoDeConcursos`: mais recente primeiro. `grupoDoSonho`: 1..25, o
// número do grupo do jogo do bicho. `hoje`: sempre exigido (nunca um
// default computado aqui) — quem chama injeta a data, a mesma disciplina
// de `Clock` já usada em ValidadorDataNascimento/VerificadorDeIdade.
data class DadosDeContribuicao(
    val hoje: LocalDate,
    val nascimento: LocalDate? = null,
    val signo: Signo? = null,
    val nome: String? = null,
    val grupoDoSonho: Int? = null,
    val historicoDeConcursos: List<List<Int>> = emptyList(),
)
