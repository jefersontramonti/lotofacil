package com.trevo.core.engine.crenca

// RF-11.4 — ordem fixa da sequência do ritual, oito amuletos (fonte:
// Docs/Trevo - Lotofácil.dc.html, array AMULETOS).
enum class Amuleto {
    TREVO,
    FERRADURA,
    ANEIS,
    MOEDAS,
    BOLA_DE_CRISTAL,
    DADOS,
    ELEFANTE,
    ESTRELA,
}

val ORDEM_DO_RITUAL: List<Amuleto> =
    listOf(
        Amuleto.TREVO,
        Amuleto.FERRADURA,
        Amuleto.ANEIS,
        Amuleto.MOEDAS,
        Amuleto.BOLA_DE_CRISTAL,
        Amuleto.DADOS,
        Amuleto.ELEFANTE,
        Amuleto.ESTRELA,
    )

// RF-11.4 — opções por amuleto: trevo tem 4 (Sorte, Prosperidade, Amor, Fé);
// moedas tem 2 (cara, coroa); os outros seis têm 3 cada. RF-11.5: os nomes
// das opções nunca indicam qual dezena escondem — só o conceito do amuleto.
enum class OpcaoDeAmuleto(
    val amuleto: Amuleto,
) {
    TREVO_SORTE(Amuleto.TREVO),
    TREVO_PROSPERIDADE(Amuleto.TREVO),
    TREVO_AMOR(Amuleto.TREVO),
    TREVO_FE(Amuleto.TREVO),
    FERRADURA_ESQUERDA(Amuleto.FERRADURA),
    FERRADURA_MEIO(Amuleto.FERRADURA),
    FERRADURA_DIREITA(Amuleto.FERRADURA),
    ANEIS_PRIMEIRO(Amuleto.ANEIS),
    ANEIS_SEGUNDO(Amuleto.ANEIS),
    ANEIS_TERCEIRO(Amuleto.ANEIS),
    MOEDAS_CARA(Amuleto.MOEDAS),
    MOEDAS_COROA(Amuleto.MOEDAS),
    BOLA_DE_CRISTAL_LUZ(Amuleto.BOLA_DE_CRISTAL),
    BOLA_DE_CRISTAL_NEVOA(Amuleto.BOLA_DE_CRISTAL),
    BOLA_DE_CRISTAL_FAISCA(Amuleto.BOLA_DE_CRISTAL),
    DADOS_VERMELHO(Amuleto.DADOS),
    DADOS_DOURADO(Amuleto.DADOS),
    DADOS_PRETO(Amuleto.DADOS),
    ELEFANTE_ERGUIDA(Amuleto.ELEFANTE),
    ELEFANTE_AO_CENTRO(Amuleto.ELEFANTE),
    ELEFANTE_CURVADA(Amuleto.ELEFANTE),
    ESTRELA_NORTE(Amuleto.ESTRELA),
    ESTRELA_ORIENTE(Amuleto.ESTRELA),
    ESTRELA_GUIA(Amuleto.ESTRELA),
}

fun opcoesDoAmuleto(amuleto: Amuleto): List<OpcaoDeAmuleto> = OpcaoDeAmuleto.entries.filter { it.amuleto == amuleto }

// RF-11.9/RF-11.10 — cada revelação vira uma dezena forçada no volante e uma
// fonte própria na explicação de origem, ao lado das crenças.
data class RevelacaoDoAmuleto(
    val amuleto: Amuleto,
    val opcao: OpcaoDeAmuleto,
    val dezena: Int,
)
