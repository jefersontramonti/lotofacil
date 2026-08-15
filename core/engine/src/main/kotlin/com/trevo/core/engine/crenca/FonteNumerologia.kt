package com.trevo.core.engine.crenca

class FonteNumerologia : FonteDeCrenca {
    override val crenca = Crenca.NUMEROLOGIA

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val nome = dados.nome
        return if (nome.isNullOrBlank()) {
            ContribuicaoDeCrenca(emptyList(), "Sem nome informado, esta crença não entra no palpite.")
        } else {
            ContribuicaoDeCrenca(dezenasDoNome(nome), "Letras do nome convertidas em dezenas.")
        }
    }
}

private const val MAXIMO_DE_DEZENAS = 8

// A=1 .. Z=26; letras além de Z (só existe a própria 26) voltam pro
// início (26 -> 1). Acentos e demais caracteres não-ASCII são ignorados,
// não normalizados — mesma regra do protótipo de referência. Para no
// oitavo valor único encontrado, na ordem em que aparecem no nome.
fun dezenasDoNome(nome: String): List<Int> {
    val vistas = LinkedHashSet<Int>()
    for (caractere in nome.uppercase()) {
        if (caractere !in 'A'..'Z') continue
        var valor = caractere.code - 'A'.code + 1
        if (valor > 25) valor -= 25
        vistas.add(valor)
        if (vistas.size == MAXIMO_DE_DEZENAS) break
    }
    return vistas.toList()
}
