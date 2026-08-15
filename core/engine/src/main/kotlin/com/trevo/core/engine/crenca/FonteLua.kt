package com.trevo.core.engine.crenca

class FonteLua : FonteDeCrenca {
    override val crenca = Crenca.LUA

    // Sempre tem dado de origem (a data de hoje nunca falta), então nunca
    // cai no caminho de exclusão de RF-02.5.
    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val fase = faseDaLuaEm(dados.hoje)
        return ContribuicaoDeCrenca(dezenasDaFaseDaLua(fase), "Fase da lua de hoje puxa estas dezenas.")
    }
}
