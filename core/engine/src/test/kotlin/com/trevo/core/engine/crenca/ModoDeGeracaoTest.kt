package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Test

class ModoDeGeracaoTest {
    private val todasAsCrencas = Crenca.entries.toSet()

    @Test
    fun modoMisticoMantemSoAsCrencasMisticas() {
        val esperado =
            setOf(Crenca.SIGNO, Crenca.NASCIMENTO, Crenca.LUA, Crenca.SONHO, Crenca.MOLDURA, Crenca.NUMEROLOGIA)

        assertEquals(esperado, crencasAtivasNoModo(ModoDeGeracao.MISTICO, todasAsCrencas))
    }

    @Test
    fun modoCientistaMantemSoAsCrencasEstatisticas() {
        val esperado =
            setOf(Crenca.QUENTES, Crenca.ATRASADOS, Crenca.PARES, Crenca.PRIMOS, Crenca.SOMA, Crenca.REPETIDAS)

        assertEquals(esperado, crencasAtivasNoModo(ModoDeGeracao.CIENTISTA, todasAsCrencas))
    }

    @Test
    fun modoDestinoMantemTodasAsSelecionadas() {
        assertEquals(todasAsCrencas, crencasAtivasNoModo(ModoDeGeracao.DESTINO, todasAsCrencas))
    }

    @Test
    fun filtrarPorModoNaoAlteraOConjuntoOriginalDeSelecionadas() {
        val selecionadas = setOf(Crenca.SIGNO, Crenca.QUENTES)

        crencasAtivasNoModo(ModoDeGeracao.MISTICO, selecionadas)

        assertEquals(setOf(Crenca.SIGNO, Crenca.QUENTES), selecionadas)
    }

    @Test
    fun modoMisticoComSelecaoSoDeCrencasEstatisticasNaoAtivaNenhuma() {
        val selecionadas = setOf(Crenca.QUENTES, Crenca.ATRASADOS)

        assertEquals(emptySet<Crenca>(), crencasAtivasNoModo(ModoDeGeracao.MISTICO, selecionadas))
    }
}
