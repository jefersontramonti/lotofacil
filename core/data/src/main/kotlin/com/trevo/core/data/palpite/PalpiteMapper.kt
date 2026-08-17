package com.trevo.core.data.palpite

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite
import java.time.Instant

private const val SEPARADOR_DEZENAS = ","
private const val SEPARADOR_CRENCAS = ";"
private const val SEPARADOR_CRENCA_DEZENAS = ":"

fun Palpite.paraEntity(
    id: Long = 0,
    criadoEm: Instant,
): PalpiteEntity =
    PalpiteEntity(
        id = id,
        dezenas = dezenas.joinToString(SEPARADOR_DEZENAS),
        dezenasFixas = dezenasFixas.joinToString(SEPARADOR_DEZENAS),
        contribuicoes = codificarContribuicoes(contribuicoes),
        forca = forca,
        criadoEmEpochMillis = criadoEm.toEpochMilli(),
    )

fun PalpiteEntity.paraDominio(): PalpiteSalvo =
    PalpiteSalvo(
        id = id,
        palpite =
            Palpite(
                dezenas = decodificarDezenas(dezenas),
                dezenasFixas = decodificarDezenas(dezenasFixas),
                contribuicoes = decodificarContribuicoes(contribuicoes),
                forca = forca,
            ),
        criadoEm = Instant.ofEpochMilli(criadoEmEpochMillis),
    )

private fun decodificarDezenas(texto: String): List<Int> =
    if (texto.isEmpty()) emptyList() else texto.split(SEPARADOR_DEZENAS).map { it.toInt() }

private fun codificarContribuicoes(contribuicoes: Map<Crenca, List<Int>>): String =
    contribuicoes.entries.joinToString(SEPARADOR_CRENCAS) { (crenca, dezenasDaCrenca) ->
        "${crenca.name}$SEPARADOR_CRENCA_DEZENAS${dezenasDaCrenca.joinToString(SEPARADOR_DEZENAS)}"
    }

private fun decodificarContribuicoes(texto: String): Map<Crenca, List<Int>> =
    if (texto.isEmpty()) {
        emptyMap()
    } else {
        texto.split(SEPARADOR_CRENCAS).associate { par ->
            val (nomeCrenca, dezenasTexto) = par.split(SEPARADOR_CRENCA_DEZENAS)
            Crenca.valueOf(nomeCrenca) to decodificarDezenas(dezenasTexto)
        }
    }
