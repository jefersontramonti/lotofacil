package com.trevo.core.engine.palpite

private const val DEZENAS_SORTEADAS_POR_CONCURSO = 15
private const val DEZENAS_TOTAIS_DA_LOTOFACIL = 25

// C(n,k) — nenhum dos tamanhos usados aqui (máx. C(25,15) = 3.268.760)
// aproxima o limite de Long, então a divisão exata a cada passo (sempre
// divisível, propriedade do triângulo de Pascal) é suficiente.
fun coeficienteBinomial(
    n: Int,
    k: Int,
): Long {
    require(n >= 0 && k in 0..n) { "k deve estar entre 0 e n (n=$n, k=$k)" }
    var resultado = 1L
    for (i in 0 until k) {
        resultado = resultado * (n - i) / (i + 1)
    }
    return resultado
}

data class Probabilidade(
    val numerador: Long,
    val denominador: Long,
)

// RF-04.4 — probabilidade real de 15 acertos do fechamento escolhido: das
// C(quantidadeDeDezenas,15) formas de sortear 15 dentro do fechamento,
// contra as C(25,15) formas possíveis de sortear 15 dentre as 25 dezenas.
// Pra um palpite de 15 dezenas dá 1/3.268.760.
fun probabilidadeDe15Acertos(quantidadeDeDezenas: Int): Probabilidade {
    require(quantidadeDeDezenas in DEZENAS_SORTEADAS_POR_CONCURSO..DEZENAS_TOTAIS_DA_LOTOFACIL) {
        "quantidadeDeDezenas deve estar entre 15 e 25, era $quantidadeDeDezenas"
    }
    return Probabilidade(
        numerador = coeficienteBinomial(quantidadeDeDezenas, DEZENAS_SORTEADAS_POR_CONCURSO),
        denominador = coeficienteBinomial(DEZENAS_TOTAIS_DA_LOTOFACIL, DEZENAS_SORTEADAS_POR_CONCURSO),
    )
}

// RF-04.10 — gerador preguiçoso das combinações de 15 dezenas dentro de um
// fechamento maior. Sequence pra quem pede só os primeiros N (Desdobramentos
// mostra um recorte) nunca materializar as até 15.504 combinações de um
// fechamento de 20.
fun combinacoesDe15(dezenas: List<Int>): Sequence<List<Int>> {
    require(dezenas.size >= DEZENAS_SORTEADAS_POR_CONCURSO) {
        "dezenas precisa ter ao menos 15 elementos, tinha ${dezenas.size}"
    }
    val ordenadas = dezenas.sorted()
    return sequence { yieldAll(combinar(ordenadas, DEZENAS_SORTEADAS_POR_CONCURSO)) }
}

private fun combinar(
    elementos: List<Int>,
    tamanho: Int,
): Sequence<List<Int>> =
    sequence {
        if (tamanho == 0) {
            yield(emptyList())
            return@sequence
        }
        if (elementos.size < tamanho) return@sequence
        for (indice in 0..(elementos.size - tamanho)) {
            val primeiro = elementos[indice]
            val restante = elementos.subList(indice + 1, elementos.size)
            for (combinacaoDoResto in combinar(restante, tamanho - 1)) {
                yield(listOf(primeiro) + combinacaoDoResto)
            }
        }
    }
