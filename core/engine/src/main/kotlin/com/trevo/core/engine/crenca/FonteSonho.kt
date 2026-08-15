package com.trevo.core.engine.crenca

class FonteSonho : FonteDeCrenca {
    override val crenca = Crenca.SONHO

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val grupo = dados.grupoDoSonho
        return if (grupo == null || grupo !in 1..25) {
            ContribuicaoDeCrenca(emptyList(), "Sem sonho informado, esta crença não entra no palpite.")
        } else {
            ContribuicaoDeCrenca(
                dezenasDoGrupoDoBicho(grupo),
                "Sonho traduzido pelo grupo $grupo do jogo do bicho — a dezena do grupo e a espelhada.",
            )
        }
    }
}
