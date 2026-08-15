package com.trevo.core.engine.crenca

class FonteSigno : FonteDeCrenca {
    override val crenca = Crenca.SIGNO

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val signo = dados.signo
        return if (signo == null) {
            ContribuicaoDeCrenca(emptyList(), "Sem data de nascimento válida, esta crença não entra no palpite.")
        } else {
            ContribuicaoDeCrenca(signo.dezenas, "Dezenas regidas pelo seu signo.")
        }
    }
}
