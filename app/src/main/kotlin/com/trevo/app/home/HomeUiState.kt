package com.trevo.app.home

import com.trevo.core.engine.crenca.FaseDaLua
import com.trevo.core.engine.crenca.GrupoDoBicho
import com.trevo.core.engine.identidade.Signo
import java.math.BigDecimal

val CUSTO_POR_JOGO: BigDecimal = BigDecimal("3.00")

data class PalpiteItemUiState(
    val id: Long,
    val numeroDoDia: Int,
    val dezenas: List<Int>,
    val forca: Int,
    val horario: String,
    // RF-03.5: dezenas que não estavam no palpite anterior do dia. `null`
    // quando este é o palpite mais antigo do dia (não há anterior).
    val dezenasNovas: List<Int>? = null,
)

data class HomeUiState(
    val carregando: Boolean = true,
    val palpitesHoje: List<PalpiteItemUiState> = emptyList(),
    val palpiteParaConfirmarExclusao: Long? = null,
    // RF-03.2 — `null` quando ainda não existe perfil salvo (o onboarding
    // ainda não gerou nenhum palpite). Nunca lança, cai num cabeçalho neutro.
    val nome: String? = null,
    val indiceDeSorte: Int? = null,
    val faseDaLua: FaseDaLua? = null,
    val signo: Signo? = null,
    val diaDaSemanaAbreviado: String? = null,
    // RF-03.3/03.10-03.13
    val crencaSonhoAtiva: Boolean = false,
    val gruposDoSonhoPreview: List<GrupoDoBicho> = emptyList(),
    val listaDeGruposExpandida: Boolean = false,
    val grupoDoSonhoConfirmadoHoje: Int? = null,
    val grupoAbertoNoDialog: GrupoDoBicho? = null,
) {
    val totalDeJogos: Int get() = palpitesHoje.size

    val custoTotal: BigDecimal get() = CUSTO_POR_JOGO.multiply(BigDecimal(totalDeJogos))
}
