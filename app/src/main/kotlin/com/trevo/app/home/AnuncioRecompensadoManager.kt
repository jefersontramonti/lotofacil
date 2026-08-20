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
    private var anuncioCarregado: RewardedAd? = null

    fun carregar(context: Context) {
        RewardedAd.load(
            context,
            ID_ANUNCIO_RECOMPENSADO_TESTE,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(anuncio: RewardedAd) {
                    anuncioCarregado = anuncio
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
        aoGanharRecompensa: () -> Unit,
        aoFechar: () -> Unit,
    ) {
        val anuncio = anuncioCarregado
        if (anuncio == null) {
            aoFechar()
            return
        }
        anuncioCarregado = null
        anuncio.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() = aoFechar()

                override fun onAdFailedToShowFullScreenContent(erro: AdError) = aoFechar()
            }
        anuncio.show(activity, OnUserEarnedRewardListener { aoGanharRecompensa() })
    }
}
