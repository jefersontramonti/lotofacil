package com.trevo.core.engine.crenca

private const val JANELA_EM_CONCURSOS = 50
private const val QUANTIDADE_DE_QUENTES = 8

class FonteQuentes : FonteDeCrenca {
    override val crenca = Crenca.QUENTES

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val historico = dados.historicoDeConcursos
        if (historico.isEmpty()) {
            return ContribuicaoDeCrenca(emptyList(), "Sem histórico de concursos, esta crença não entra no palpite.")
        }
        val janela = historico.take(JANELA_EM_CONCURSOS)
        val frequencia = IntArray(26)
        janela.forEach { concurso -> concurso.forEach { n -> if (n in 1..25) frequencia[n]++ } }
        // Empate de frequência desempata pela menor dezena, pra saída
        // determinística — nunca depende da ordem de iteração do histórico.
        val maisSorteadas =
            (1..25)
                .sortedWith(compareByDescending<Int> { frequencia[it] }.thenBy { it })
                .take(QUANTIDADE_DE_QUENTES)
        return ContribuicaoDeCrenca(maisSorteadas, "As mais sorteadas nos últimos ${janela.size} concursos.")
    }
}
