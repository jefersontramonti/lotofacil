package com.trevo.core.engine.crenca

import java.time.LocalDate

class FonteNascimento : FonteDeCrenca {
    override val crenca = Crenca.NASCIMENTO

    override fun contribuir(dados: DadosDeContribuicao): ContribuicaoDeCrenca {
        val nascimento = dados.nascimento
        return if (nascimento == null) {
            ContribuicaoDeCrenca(emptyList(), "Sem data de nascimento válida, esta crença não entra no palpite.")
        } else {
            ContribuicaoDeCrenca(dezenasDoNascimento(nascimento), "Do dia, do mês e da redução do ano de nascimento.")
        }
    }
}

// Cada par de dígitos de ddMMaaaa vira uma dezena; pares acima de 25 são
// reduzidos somando os próprios dígitos até caber em 1..25 (ex.: "78" ->
// 7+8 = 15). Duplicatas são descartadas mantendo a primeira ocorrência.
fun dezenasDoNascimento(data: LocalDate): List<Int> {
    val digitos = "%02d%02d%04d".format(data.dayOfMonth, data.monthValue, data.year)
    val vistas = LinkedHashSet<Int>()
    digitos.chunked(2).forEach { par ->
        var valor = par.toInt()
        while (valor > 25) valor = valor.toString().sumOf { it.digitToInt() }
        if (valor in 1..25) vistas.add(valor)
    }
    return vistas.toList()
}
