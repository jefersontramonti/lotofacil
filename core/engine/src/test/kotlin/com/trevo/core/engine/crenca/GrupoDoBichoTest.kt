package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrupoDoBichoTest {
    private val expressoesProibidas = listOf("aumenta", "garante", "mais chance", "melhora sua chance", "prevê")

    @Test
    fun existemExatamente25GruposNumeradosDe1A25SemBuraco() {
        assertEquals((1..25).toList(), GRUPOS_DO_BICHO.map { it.numero })
    }

    @Test
    fun cadaGrupoTemNomeEmojiELeituraNaoVazios() {
        GRUPOS_DO_BICHO.forEach { grupo ->
            assertTrue("grupo ${grupo.numero} sem nome", grupo.nome.isNotBlank())
            assertTrue("grupo ${grupo.numero} sem emoji", grupo.emoji.isNotBlank())
            assertTrue("grupo ${grupo.numero} sem leitura popular", grupo.leituraPopular.isNotBlank())
        }
    }

    @Test
    fun nenhumaLeituraPopularPrometeAumentoDeChanceOuSePassaPorPrevisao() {
        GRUPOS_DO_BICHO.forEach { grupo ->
            val leituraEmMinusculas = grupo.leituraPopular.lowercase()
            expressoesProibidas.forEach { expressaoProibida ->
                assertTrue(
                    "grupo ${grupo.numero} (${grupo.nome}) não pode conter \"$expressaoProibida\", " +
                        "mas era: \"${grupo.leituraPopular}\"",
                    !leituraEmMinusculas.contains(expressaoProibida),
                )
            }
        }
    }

    @Test
    fun dezenasDoGrupoDoBichoContinuaSendoAFonteDasDezenasDeCadaGrupo() {
        GRUPOS_DO_BICHO.forEach { grupo ->
            assertTrue(dezenasDoGrupoDoBicho(grupo.numero).isNotEmpty())
        }
    }
}
