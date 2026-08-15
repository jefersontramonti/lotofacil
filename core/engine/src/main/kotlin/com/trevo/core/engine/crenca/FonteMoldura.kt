package com.trevo.core.engine.crenca

class FonteMoldura : FonteDeCrenca {
    override val crenca = Crenca.MOLDURA

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca =
        ContribuicaoDeCrenca(DEZENAS_DA_MOLDURA, "Reforça a borda da cartela, contra o miolo.")
}

// Cartela 5x5 (linhas e colunas de 0 a 4): a moldura é a primeira/última
// linha ou a primeira/última coluna.
val DEZENAS_DA_MOLDURA: List<Int> =
    (1..25).filter { dezena ->
        val linha = (dezena - 1) / 5
        val coluna = (dezena - 1) % 5
        linha == 0 || linha == 4 || coluna == 0 || coluna == 4
    }
