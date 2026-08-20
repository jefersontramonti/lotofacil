package com.trevo.app.ritual

import com.trevo.core.engine.crenca.Amuleto
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import com.trevo.core.engine.palpite.TamanhoDeFechamento

sealed interface RitualUiState {
    data object Carregando : RitualUiState

    // RF-11.4/RF-11.8 — `indice`/`total` alimentam a trilha de progresso;
    // `reveladas` é o que já saiu (dezenas nunca aparecem até a escolha, RF-11.5).
    data class Escolha(
        val amuletoAtual: Amuleto,
        val indice: Int,
        val total: Int,
        val reveladas: List<RevelacaoDoAmuleto>,
    ) : RitualUiState

    // RF-11.6 — a dezena já foi sorteada (`ultimaRevelacao`), a tela mostra a
    // animação de giro/halo; avança sozinha (ver TelaRitual).
    data class Revelando(
        val ultimaRevelacao: RevelacaoDoAmuleto,
        val reveladas: List<RevelacaoDoAmuleto>,
    ) : RitualUiState

    // RF-11.9. `tamanho` — `null` é o palpite padrão de 15 (as reveladas +
    // as que vêm de crenças/estatística); DEZESSEIS/DEZOITO/VINTE é
    // fechamento (RF-02.8), disponível no ritual só com `isPro` — mesmo
    // padrão de "visível mas bloqueado" já usado em CrencasUiState/RF-01.8 e
    // no SeletorDeFechamento de TelaDetalhe, sem paywall real ainda (RF-09).
    data class Resumo(
        val reveladas: List<RevelacaoDoAmuleto>,
        val quantidadeDeOutrasDezenas: Int,
        val tamanho: TamanhoDeFechamento? = null,
        val isPro: Boolean = false,
        val montandoPalpite: Boolean = false,
    ) : RitualUiState
}
