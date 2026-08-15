package com.trevo.core.engine.crenca

class FonteSoma : FonteDeCrenca {
    override val crenca = Crenca.SOMA

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca =
        ContribuicaoDeCrenca(DEZENAS_SOMA_MEIO, "Dezenas do meio para levar a soma à faixa de 180 a 210.")
}

val DEZENAS_SOMA_MEIO: List<Int> = (9..16).toList()
