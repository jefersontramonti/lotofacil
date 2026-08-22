package com.trevo.app.home

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.UUID

// RF-09.2/09.3 — só este formato (recompensado) em todo o app, nunca banner
// nem intersticial; RF-09.3 é cumprido por omissão, não há outro lugar que
// chame o SDK de anúncios. ID de TESTE do Google (funciona sem conta
// AdMob) — trocar pelo ad unit real antes de publicar, junto do App ID no
// AndroidManifest.xml.
private const val ID_ANUNCIO_RECOMPENSADO_TESTE = "ca-app-pub-3940256099942544/5224354917"

// Sem Hilt de propósito: não depende de nada além do SDK do AdMob, e
// `exibir` precisa de Activity (não só Context) — instanciado com
// `remember` na Composable que o usa (TrevoNavHost), mesmo espírito do
// launcher de permissão de RF-07.7.
class AnuncioRecompensadoManager {
    // Achado de auditoria de segurança: nada aqui substitui verificação
    // server-side (SSV) — sem backend, não há como confirmar fora do
    // aparelho que o anúncio foi assistido de verdade. O token existe só
    // pra fechar o caminho mais barato de fraude (um botão reconectado
    // direto em `HomeViewModel.aoAnuncioRecompensado`, num APK adulterado,
    // sem passar pelo carregamento real do anúncio): pra creditar, quem
    // chama precisa produzir o mesmo token que `aoCarregar` emitiu quando o
    // SDK do AdMob de fato carregou um anúncio — ver HomeViewModel e
    // PROJECT_STATE.md (dívida de SSV registrada). Não resiste a
    // instrumentação em tempo de execução (Frida/Xposed) num aparelho
    // rooteado, só a uma reconexão estática do botão.
    private data class AnuncioCarregado(
        val anuncio: RewardedAd,
        val token: String,
    )

    private var anuncioCarregado: AnuncioCarregado? = null

    fun carregar(
        context: Context,
        aoCarregar: (token: String) -> Unit,
    ) {
        RewardedAd.load(
            context,
            ID_ANUNCIO_RECOMPENSADO_TESTE,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(anuncio: RewardedAd) {
                    val token = UUID.randomUUID().toString()
                    anuncioCarregado = AnuncioCarregado(anuncio, token)
                    aoCarregar(token)
                }

                override fun onAdFailedToLoad(erro: LoadAdError) {
                    anuncioCarregado = null
                }
            },
        )
    }

    // Sem anúncio pronto (ainda carregando/falhou), cai direto pra
    // `aoFechar` — nunca trava a tela nem finge uma recompensa que não
    // aconteceu.
    fun exibir(
        activity: Activity,
        aoGanharRecompensa: (token: String) -> Unit,
        aoFechar: () -> Unit,
    ) {
        val carregado = anuncioCarregado
        if (carregado == null) {
            aoFechar()
            return
        }
        anuncioCarregado = null
        carregado.anuncio.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() = aoFechar()

                override fun onAdFailedToShowFullScreenContent(erro: AdError) = aoFechar()
            }
        carregado.anuncio.show(activity, OnUserEarnedRewardListener { aoGanharRecompensa(carregado.token) })
    }
}
