package com.trevo.app.som

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.trevo.app.R

// Um único SoundPool carrega os 26 efeitos curtos de Efeito uma vez (cada
// um com poucas dezenas de KB); o loop ambiente da geração usa MediaPlayer
// à parte porque SoundPool não é feito pra áudio longo em loop contínuo.
// Instanciado uma vez em TrevoNavHost (remember) e liberado quando o app
// fecha (DisposableEffect) — não é singleton do Hilt de propósito, mesmo
// padrão não-Hilt já usado por AnuncioRecompensadoManager.
class TocadorDeSom(
    context: Context,
) {
    private val appContext = context.applicationContext

    // load() é assíncrono — tocar um som antes do callback de conclusão
    // silenciosamente não faz nada (SoundPool só loga "not READY"). idsProntos
    // rastreia quais amostras já decodificaram; tocar() ignora o pedido até lá
    // em vez de vazar o warning pro usuário como som que nunca chega a soar.
    private val idsProntos = mutableSetOf<Int>()

    private val soundPool =
        SoundPool
            .Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()
            .apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) idsProntos.add(sampleId)
                }
            }

    private val idsCarregados: Map<Efeito, Int> =
        Efeito.entries.associateWith { efeito -> soundPool.load(appContext, efeito.resId, 1) }

    private var mediaPlayerAmbiente: MediaPlayer? = null

    fun tocar(efeito: Efeito) {
        val id = idsCarregados[efeito] ?: return
        if (id !in idsProntos) return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun iniciarAmbiente() {
        if (mediaPlayerAmbiente != null) return
        mediaPlayerAmbiente =
            MediaPlayer.create(appContext, R.raw.loop_geracao_ambiente)?.apply {
                isLooping = true
                start()
            }
    }

    fun pararAmbiente() {
        mediaPlayerAmbiente?.release()
        mediaPlayerAmbiente = null
    }

    fun liberar() {
        pararAmbiente()
        soundPool.release()
    }
}
