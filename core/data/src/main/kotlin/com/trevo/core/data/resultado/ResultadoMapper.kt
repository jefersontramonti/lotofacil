package com.trevo.core.data.resultado

import com.trevo.core.engine.resultado.FaixaDePremio
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.ProximoConcurso
import com.trevo.core.engine.resultado.Resultado
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATO_DATA_API = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Regra estrutural do jogo (não dado de sorteio — CLAUDE.md §8 só proíbe
// inventar resultado/prêmio, não a estrutura fixa de faixas da Lotofácil),
// confirmada contra o concurso 3457 real via WebFetch: faixa 1 = 15
// acertos, faixa 2 = 14, faixa 3 = 13, faixa 4 = 12, faixa 5 = 11.
private val ACERTOS_POR_FAIXA = mapOf(1 to 15, 2 to 14, 3 to 13, 4 to 12, 5 to 11)

fun ResultadoDto.paraDominio(): Resultado =
    Resultado(
        numero = numero,
        dataApuracao = LocalDate.parse(dataApuracao, FORMATO_DATA_API),
        dezenasSorteadas = listaDezenas.map { it.toInt() },
        faixasDePremio = listaRateioPremio.mapNotNull { it.paraDominio() },
        acumulado = acumulado,
        origem = OrigemDoResultado.API,
        proximoConcurso = paraProximoConcurso(),
    )

// Os 4 campos chegam juntos ou não chegam — schema real confirmado contra
// a API (não documentado em nenhum doc do projeto): numeroConcursoProximo,
// dataProximoConcurso, valorEstimadoProximoConcurso, valorAcumuladoProximoConcurso.
private fun ResultadoDto.paraProximoConcurso(): ProximoConcurso? {
    val numeroProximo = numeroConcursoProximo ?: return null
    val dataProximo = dataProximoConcurso?.let { LocalDate.parse(it, FORMATO_DATA_API) } ?: return null
    val valorEstimado = valorEstimadoProximoConcurso ?: return null
    val valorAcumulado = valorAcumuladoProximoConcurso ?: return null
    return ProximoConcurso(
        numero = numeroProximo,
        data = dataProximo,
        valorEstimadoPremio = BigDecimal.valueOf(valorEstimado),
        valorAcumulado = BigDecimal.valueOf(valorAcumulado),
    )
}

// valorPremio chega como número JSON puro (Double) — convertido pra
// BigDecimal aqui, na borda, antes de entrar em qualquer lógica de
// domínio (CLAUDE.md §5 proíbe Double em valor monetário).
private fun RateioDto.paraDominio(): FaixaDePremio? {
    val acertos = ACERTOS_POR_FAIXA[faixa] ?: return null
    return FaixaDePremio(
        acertosNecessarios = acertos,
        numeroDeGanhadores = numeroDeGanhadores,
        valorPremio = BigDecimal.valueOf(valorPremio),
    )
}

private const val SEPARADOR_DEZENAS = ","
private const val SEPARADOR_FAIXAS = ";"
private const val SEPARADOR_CAMPOS_FAIXA = ":"

fun Resultado.paraEntity(id: Long = 0): ResultadoEntity =
    ResultadoEntity(
        id = id,
        numero = numero,
        dataApuracaoIso = dataApuracao.toString(),
        dezenasSorteadas = dezenasSorteadas.joinToString(SEPARADOR_DEZENAS),
        faixas = codificarFaixas(faixasDePremio),
        acumulado = acumulado,
        origem = origem.name,
        proximoConcurso = codificarProximoConcurso(proximoConcurso),
    )

fun ResultadoEntity.paraDominio(): Resultado =
    Resultado(
        numero = numero,
        dataApuracao = LocalDate.parse(dataApuracaoIso),
        dezenasSorteadas = decodificarDezenas(dezenasSorteadas),
        faixasDePremio = decodificarFaixas(faixas),
        acumulado = acumulado,
        origem = OrigemDoResultado.valueOf(origem),
        proximoConcurso = decodificarProximoConcurso(proximoConcurso),
    )

private fun decodificarDezenas(texto: String): List<Int> =
    if (texto.isEmpty()) emptyList() else texto.split(SEPARADOR_DEZENAS).map { it.toInt() }

private fun codificarFaixas(faixas: List<FaixaDePremio>): String =
    faixas.joinToString(SEPARADOR_FAIXAS) { faixa ->
        listOf(
            faixa.acertosNecessarios,
            faixa.numeroDeGanhadores,
            faixa.valorPremio,
        ).joinToString(SEPARADOR_CAMPOS_FAIXA)
    }

private fun decodificarFaixas(texto: String): List<FaixaDePremio> =
    if (texto.isEmpty()) {
        emptyList()
    } else {
        texto.split(SEPARADOR_FAIXAS).map { par ->
            val (acertos, ganhadores, valor) = par.split(SEPARADOR_CAMPOS_FAIXA)
            FaixaDePremio(
                acertosNecessarios = acertos.toInt(),
                numeroDeGanhadores = ganhadores.toLong(),
                valorPremio = BigDecimal(valor),
            )
        }
    }

private fun codificarProximoConcurso(proximoConcurso: ProximoConcurso?): String? =
    proximoConcurso?.let {
        listOf(
            it.numero,
            it.data,
            it.valorEstimadoPremio,
            it.valorAcumulado,
        ).joinToString(SEPARADOR_CAMPOS_FAIXA)
    }

private fun decodificarProximoConcurso(texto: String?): ProximoConcurso? {
    if (texto.isNullOrEmpty()) return null
    val (numero, data, valorEstimado, valorAcumulado) = texto.split(SEPARADOR_CAMPOS_FAIXA)
    return ProximoConcurso(
        numero = numero.toInt(),
        data = LocalDate.parse(data),
        valorEstimadoPremio = BigDecimal(valorEstimado),
        valorAcumulado = BigDecimal(valorAcumulado),
    )
}
