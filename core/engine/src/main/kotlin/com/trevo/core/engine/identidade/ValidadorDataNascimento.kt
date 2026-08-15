package com.trevo.core.engine.identidade

import java.time.Clock
import java.time.LocalDate

private val FORMATO_DATA_ESTRITO = Regex("""^(\d{2})/(\d{2})/(\d{4})$""")
private const val ANO_MINIMO = 1900

class ValidadorDataNascimento(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun validar(texto: String): ResultadoDataNascimento {
        val textoNormalizado = texto.trim()
        if (textoNormalizado.isEmpty()) {
            return ResultadoDataNascimento.Invalida(ErroDataNascimento.VAZIO)
        }

        val casamento =
            FORMATO_DATA_ESTRITO.matchEntire(textoNormalizado)
                ?: return ResultadoDataNascimento.Invalida(ErroDataNascimento.FORMATO_INVALIDO)
        val (diaTexto, mesTexto, anoTexto) = casamento.destructured
        val dia = diaTexto.toInt()
        val mes = mesTexto.toInt()
        val ano = anoTexto.toInt()

        if (mes !in 1..12) {
            return ResultadoDataNascimento.Invalida(ErroDataNascimento.MES_INVALIDO)
        }

        if (dia !in 1..diasDoMes(mes, ano)) {
            return ResultadoDataNascimento.Invalida(ErroDataNascimento.DIA_INEXISTENTE)
        }

        val data = LocalDate.of(ano, mes, dia)
        val hoje = LocalDate.now(clock)
        if (ano < ANO_MINIMO || data.isAfter(hoje)) {
            return ResultadoDataNascimento.Invalida(ErroDataNascimento.FORA_DO_INTERVALO)
        }

        return ResultadoDataNascimento.Valida(data)
    }
}

internal fun ehAnoBissexto(ano: Int): Boolean = (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0

private fun diasDoMes(
    mes: Int,
    ano: Int,
): Int =
    when (mes) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (ehAnoBissexto(ano)) 29 else 28
        else -> error("mês $mes deveria ter sido validado antes de chegar aqui")
    }
