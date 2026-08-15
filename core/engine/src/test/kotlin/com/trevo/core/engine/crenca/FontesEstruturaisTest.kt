package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// Fontes sem dependência de dado do apostador — nunca caem em RF-02.5,
// sempre têm 16 (moldura), 12 (pares) ou 8 (soma) dezenas fixas.
class FontesEstruturaisTest {
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun molduraSaoAsBordasDaCartela5x5() {
        val esperado = listOf(1, 2, 3, 4, 5, 6, 10, 11, 15, 16, 20, 21, 22, 23, 24, 25)

        assertEquals(esperado, DEZENAS_DA_MOLDURA)
        assertEquals(esperado, FonteMoldura().contribuir(dadosBase).dezenas)
    }

    @Test
    fun paresSaoTodasAsDezenasPares() {
        val esperado = (2..24 step 2).toList()

        assertEquals(esperado, DEZENAS_PARES)
        assertEquals(esperado, FontePares().contribuir(dadosBase).dezenas)
        assertTrue(DEZENAS_PARES.all { it % 2 == 0 })
    }

    @Test
    fun primosEFibonacciEUniaoDosDoisConjuntos() {
        val esperado = listOf(1, 2, 3, 5, 7, 8, 11, 13, 17, 19, 21, 23)

        assertEquals(esperado, DEZENAS_PRIMOS_E_FIBONACCI)
        assertEquals(esperado, FontePrimos().contribuir(dadosBase).dezenas)
    }

    @Test
    fun somaSaoAsOitoDezenasDoMeioDoIntervalo() {
        val esperado = (9..16).toList()

        assertEquals(esperado, DEZENAS_SOMA_MEIO)
        assertEquals(esperado, FonteSoma().contribuir(dadosBase).dezenas)
    }

    @Test
    fun luaSempreTemContribuicaoNuncaCaiEmRf025() {
        val contribuicao = FonteLua().contribuir(dadosBase)

        assertTrue(contribuicao.dezenas.isNotEmpty())
    }
}
