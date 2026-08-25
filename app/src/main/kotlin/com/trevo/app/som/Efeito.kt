package com.trevo.app.som

import androidx.annotation.RawRes
import com.trevo.app.R
import com.trevo.core.engine.crenca.Amuleto

// Um efeito por linha de Docs/audios-necessarios.md — cada um tem um prompt
// próprio documentado lá, gerado no ElevenLabs. O loop ambiente da geração
// (loop_geracao_ambiente) fica fora daqui: MediaPlayer, não SoundPool (ver
// TocadorDeSom.iniciarAmbiente/pararAmbiente).
enum class Efeito(
    @RawRes val resId: Int,
) {
    AMULETO_TREVO(R.raw.sfx_amuleto_trevo),
    AMULETO_FERRADURA(R.raw.sfx_amuleto_ferradura),
    AMULETO_ANEIS(R.raw.sfx_amuleto_aneis),
    AMULETO_MOEDAS(R.raw.sfx_amuleto_moedas),
    AMULETO_BOLA_DE_CRISTAL(R.raw.sfx_amuleto_bola_cristal),
    AMULETO_DADOS(R.raw.sfx_amuleto_dados),
    AMULETO_ELEFANTE(R.raw.sfx_amuleto_elefante),
    AMULETO_ESTRELA(R.raw.sfx_amuleto_estrela),
    RITUAL_ESCOLHA(R.raw.sfx_ritual_escolha),
    RITUAL_REVELACAO(R.raw.sfx_ritual_revelacao),
    RITUAL_REFAZER(R.raw.sfx_ritual_refazer),
    RITUAL_BLOQUEADO(R.raw.sfx_ritual_bloqueado),
    RITUAL_MONTAR(R.raw.sfx_ritual_montar),
    GERACAO_FRASE(R.raw.sfx_geracao_frase),
    GERACAO_CONCLUIDA(R.raw.sfx_geracao_concluida),
    SONHO_CONFIRMAR(R.raw.sfx_sonho_confirmar),
    MODO_SELECIONAR(R.raw.sfx_modo_selecionar),
    PULL_REFRESH_PUXAR(R.raw.sfx_pull_refresh_puxar),
    PULL_REFRESH_CONCLUIDO(R.raw.sfx_pull_refresh_concluido),
    CONFERENCIA_RESULTADO_PRONTO(R.raw.sfx_conferencia_resultado_pronto),
    CONFERENCIA_ACERTO(R.raw.sfx_conferencia_acerto),
    CONFERENCIA_GRADE_CONFIRMAR(R.raw.sfx_conferencia_grade_confirmar),
    ABERTURA_BOAS_VINDAS(R.raw.sfx_abertura_boas_vindas),
    CRENCA_MARCAR(R.raw.sfx_crenca_marcar),
    ACAO_CONFIRMAR(R.raw.sfx_acao_confirmar),
    ACAO_EXCLUIR(R.raw.sfx_acao_excluir),
}

fun efeitoDoAmuleto(amuleto: Amuleto): Efeito =
    when (amuleto) {
        Amuleto.TREVO -> Efeito.AMULETO_TREVO
        Amuleto.FERRADURA -> Efeito.AMULETO_FERRADURA
        Amuleto.ANEIS -> Efeito.AMULETO_ANEIS
        Amuleto.MOEDAS -> Efeito.AMULETO_MOEDAS
        Amuleto.BOLA_DE_CRISTAL -> Efeito.AMULETO_BOLA_DE_CRISTAL
        Amuleto.DADOS -> Efeito.AMULETO_DADOS
        Amuleto.ELEFANTE -> Efeito.AMULETO_ELEFANTE
        Amuleto.ESTRELA -> Efeito.AMULETO_ESTRELA
    }
