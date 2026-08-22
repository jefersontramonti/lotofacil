package com.trevo.core.data.resultado

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

// Achado de auditoria de segurança: a API da Caixa é pública e pode mudar
// sem aviso — `paraDominio()` precisa rejeitar um payload malformado em vez
// de deixar a conferência do usuário rodar contra dezenas erradas.
class ResultadoMapperTest {
    private val dtoValido =
        ResultadoDto(
            numero = 3457,
            dataApuracao = "31/07/2025",
            listaDezenas = (1..15).map { "%02d".format(it) },
        )

    @Test
    fun payloadValidoMapeiaAsQuinzeDezenas() {
        val resultado = dtoValido.paraDominio()

        assertEquals((1..15).toList(), resultado.dezenasSorteadas)
    }

    @Test
    fun payloadComMenosDeQuinzeDezenasLancaExcecao() {
        val dto = dtoValido.copy(listaDezenas = (1..14).map { "%02d".format(it) })

        assertThrows(IllegalStateException::class.java) { dto.paraDominio() }
    }

    @Test
    fun payloadComMaisDeQuinzeDezenasLancaExcecao() {
        val dto = dtoValido.copy(listaDezenas = (1..16).map { "%02d".format(it) })

        assertThrows(IllegalStateException::class.java) { dto.paraDominio() }
    }

    @Test
    fun payloadComDezenaZeroLancaExcecao() {
        val dto = dtoValido.copy(listaDezenas = listOf("00") + (2..15).map { "%02d".format(it) })

        assertThrows(IllegalStateException::class.java) { dto.paraDominio() }
    }

    @Test
    fun payloadComDezenaAcimaDeVinteECincoLancaExcecao() {
        val dto = dtoValido.copy(listaDezenas = listOf("26") + (2..15).map { "%02d".format(it) })

        assertThrows(IllegalStateException::class.java) { dto.paraDominio() }
    }

    @Test
    fun payloadComDezenaRepetidaLancaExcecao() {
        val dto = dtoValido.copy(listaDezenas = listOf("01") + (1..14).map { "%02d".format(it) })

        assertThrows(IllegalStateException::class.java) { dto.paraDominio() }
    }
}
