package com.trevo.core.engine.crenca

private const val JANELA_MINIMA_EM_CONCURSOS = 6

class FonteAtrasados : FonteDeCrenca {
    override val crenca = Crenca.ATRASADOS

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val historico = dados.historicoDeConcursos
        if (historico.size < JANELA_MINIMA_EM_CONCURSOS) {
            return ContribuicaoDeCrenca(
                emptyList(),
                "Sem histórico de ao menos $JANELA_MINIMA_EM_CONCURSOS concursos, esta crença não entra no palpite.",
            )
        }
        val janela = historico.take(JANELA_MINIMA_EM_CONCURSOS)
        val sorteadasNaJanela = janela.flatten().toSet()
        val atrasadas = (1..25).filter { it !in sorteadasNaJanela }
        return ContribuicaoDeCrenca(atrasadas, "Sem sair há pelo menos $JANELA_MINIMA_EM_CONCURSOS concursos.")
    }
}
