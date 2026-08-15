package com.trevo.core.engine.crenca

class FonteRepetidas : FonteDeCrenca {
    override val crenca = Crenca.REPETIDAS

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val ultimoConcurso = dados.historicoDeConcursos.firstOrNull()
        return if (ultimoConcurso == null) {
            ContribuicaoDeCrenca(emptyList(), "Sem concurso anterior conferido, esta crença não entra no palpite.")
        } else {
            ContribuicaoDeCrenca(ultimoConcurso.sorted(), "Dezenas repetidas do concurso anterior.")
        }
    }
}
