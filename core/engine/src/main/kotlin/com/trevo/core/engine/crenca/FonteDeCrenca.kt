package com.trevo.core.engine.crenca

// RF-02.5: uma fonte sem dado de origem válido devolve lista vazia e o
// motivo em `explicacao` — nunca lança exceção, nunca usa valor padrão
// silencioso.
interface FonteDeCrenca {
    val crenca: Crenca

    fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca
}
