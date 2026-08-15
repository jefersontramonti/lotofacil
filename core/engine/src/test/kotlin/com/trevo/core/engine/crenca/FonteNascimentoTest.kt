package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FonteNascimentoTest {
    private val fonte = FonteNascimento()
    private val dadosBase = DadosDeContribuicao(hoje = LocalDate.of(2026, 8, 14))

    @Test
    fun semNascimentoDevolveListaVaziaEMotivo() {
        val contribuicao = fonte.contribuir(dadosBase.copy(nascimento = null))

        assertTrue(contribuicao.dezenas.isEmpty())
        assertTrue(contribuicao.explicacao.isNotBlank())
    }

    @Test
    fun dezenasDoNascimentoQuebraDdMmAaaaEmParesDeDigitos() {
        // "14/07/1978" -> dígitos "14071978" -> pares "14","07","19","78".
        // "78" > 25, reduzido pela soma dos dígitos: 7+8 = 15.
        assertEquals(listOf(14, 7, 19, 15), dezenasDoNascimento(LocalDate.of(1978, 7, 14)))
    }

    @Test
    fun parAcimaDe25EReduzidoSomandoOsProprios0Digitos() {
        // "01/01/1999" -> dígitos "01011999" -> pares "01","01","19","99".
        // "99" > 25, reduzido: 9+9 = 18. O segundo "01" repete o primeiro
        // (mesmo valor 1) e é descartado pela deduplicação.
        assertEquals(listOf(1, 19, 18), dezenasDoNascimento(LocalDate.of(1999, 1, 1)))
    }

    @Test
    fun paresDuplicadosSaoDescartadosMantendoAPrimeiraOcorrencia() {
        // "01/01/2001" -> dígitos "01012001" -> pares "01","01","20","01".
        assertEquals(listOf(1, 20), dezenasDoNascimento(LocalDate.of(2001, 1, 1)))
    }

    @Test
    fun fonteDevolveExatamenteODeDezenasDoNascimento() {
        val nascimento = LocalDate.of(1978, 7, 14)

        val contribuicao = fonte.contribuir(dadosBase.copy(nascimento = nascimento))

        assertEquals(dezenasDoNascimento(nascimento), contribuicao.dezenas)
    }
}
