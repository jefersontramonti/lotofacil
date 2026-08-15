package com.trevo.app.onboarding

import com.trevo.core.engine.crenca.Crenca
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrencasUiStateTest {
    @Test
    fun comMenosDeTresSelecionadasNenhumaCrencaFicaBloqueada() {
        val estado = CrencasUiState(selecionadas = setOf(Crenca.SIGNO, Crenca.LUA))

        Crenca.entries.forEach { crenca ->
            assertFalse(estado.crencaBloqueada(crenca))
        }
    }

    @Test
    fun comExatamenteTresSelecionadasAsDemaisFicamBloqueadas() {
        val tresSelecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO)
        val estado = CrencasUiState(selecionadas = tresSelecionadas)

        Crenca.entries.filter { it !in tresSelecionadas }.forEach { crenca ->
            assertTrue(estado.crencaBloqueada(crenca))
        }
    }

    @Test
    fun crencaJaSelecionadaNuncaFicaBloqueadaMesmoNoLimite() {
        val tresSelecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO)
        val estado = CrencasUiState(selecionadas = tresSelecionadas)

        tresSelecionadas.forEach { crenca ->
            assertFalse(estado.crencaBloqueada(crenca))
        }
    }

    @Test
    fun usuarioProNuncaTemCrencaBloqueada() {
        val estado =
            CrencasUiState(
                selecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO, Crenca.MOLDURA, Crenca.PARES),
                isPro = true,
            )

        Crenca.entries.forEach { crenca ->
            assertFalse(estado.crencaBloqueada(crenca))
        }
    }

    @Test
    fun comMaisDeTresSelecionadasAsNaoSelecionadasContinuamBloqueadas() {
        val estado =
            CrencasUiState(
                selecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO, Crenca.MOLDURA, Crenca.PARES),
            )

        assertTrue(estado.crencaBloqueada(Crenca.PRIMOS))
    }
}
