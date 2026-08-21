package com.trevo.app.home

import com.trevo.core.engine.crenca.FaseDaLua
import com.trevo.core.engine.crenca.GrupoDoBicho
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.identidade.Signo
import java.math.BigDecimal

// Docs/tabelavalores.md — tabela oficial de preços da Lotofácil. Jogo de
// 15 dezenas custa R$ 3,50; fechamentos maiores multiplicam por C(n,15)
// jogos de 15 equivalentes (RF-04.9) e batem exatamente com a tabela
// (ex.: 18 dezenas = C(18,15) × 3,50 = 816 × 3,50 = R$ 2.856,00).
val CUSTO_POR_JOGO: BigDecimal = BigDecimal("3.50")

data class PalpiteItemUiState(
    val id: Long,
    val numeroDoDia: Int,
    val dezenas: List<Int>,
    val forca: Int,
    val horario: String,
    // RF-03.5: dezenas que não estavam no palpite anterior do dia. `null`
    // quando este é o palpite mais antigo do dia (não há anterior).
    val dezenasNovas: List<Int>? = null,
    // RF-11.13 — `null` pros palpites de antes do RF-11/gerados pelo onboarding.
    val modo: ModoDeGeracao? = null,
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
    // RF-11.1 — seleção transiente, não persistida: reabrir a Home volta
    // pro modo padrão. RF-11.2: só as crenças filtradas por `modoSelecionado`
    // entram no próximo palpite gerado por aqui.
    val modoSelecionado: ModoDeGeracao = ModoDeGeracao.MISTICO,
    // RF-09.1/RF-09.2/RF-03.9
    val isPro: Boolean = false,
    val palpitesGratisRestantesHoje: Int = 1,
    // RF-09.2 — máx. 2 anúncios recompensados/dia (espelha
    // LIMITE_ANUNCIOS_POR_DIA de PreferenciasRepositoryImpl); zero esconde
    // o botão de anúncio, sobrando só a chamada pra assinar.
    val anunciosDisponiveisHoje: Int = 2,
    // RF-03.1 — null até o primeiro resultado real ser buscado (RF-05);
    // nunca inventado, ver nota em HomeViewModel.montarUiState.
    val numeroDoConcursoCorrente: Int? = null,
) {
    val totalDeJogos: Int get() = palpitesHoje.size

    val custoTotal: BigDecimal get() = CUSTO_POR_JOGO.multiply(BigDecimal(totalDeJogos))

    val semPalpiteGratisHoje: Boolean get() = !isPro && palpitesGratisRestantesHoje <= 0
}
