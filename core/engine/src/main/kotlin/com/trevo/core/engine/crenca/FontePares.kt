package com.trevo.core.engine.crenca

class FontePares : FonteDeCrenca {
    override val crenca = Crenca.PARES

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca =
        ContribuicaoDeCrenca(DEZENAS_PARES, "Ajuste para o equilíbrio de sete pares e oito ímpares.")
}

val DEZENAS_PARES: List<Int> = (1..25).filter { it % 2 == 0 }
