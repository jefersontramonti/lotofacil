package com.trevo.core.engine.crenca

import kotlin.math.sqrt

class FontePrimos : FonteDeCrenca {
    override val crenca = Crenca.PRIMOS

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca =
        ContribuicaoDeCrenca(DEZENAS_PRIMOS_E_FIBONACCI, "Primos e a sequência de Fibonacci.")
}

private fun ehPrimo(n: Int): Boolean {
    if (n < 2) return false
    for (divisor in 2..sqrt(n.toDouble()).toInt()) {
        if (n % divisor == 0) return false
    }
    return true
}

private fun fibonacciAte(limite: Int): Set<Int> {
    val termos = mutableSetOf<Int>()
    var anterior = 1
    var atual = 1
    while (anterior <= limite) {
        termos.add(anterior)
        val proximo = anterior + atual
        anterior = atual
        atual = proximo
    }
    return termos
}

val DEZENAS_PRIMOS_E_FIBONACCI: List<Int> =
    ((1..25).filter(::ehPrimo).toSet() + fibonacciAte(25)).sorted()
