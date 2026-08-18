package com.trevo.core.engine.crenca

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IndiceDeSorteTest {
    @Test
    fun mesmoDiaEMesmoNomeSempreProduzOMesmoIndice() {
        val data = LocalDate.of(2026, 8, 17)

        assertEquals(indiceDeSorteDoDia("Marlene", data), indiceDeSorteDoDia("Marlene", data))
    }

    @Test
    fun mesmaEntradaProduzASaidaExata() {
        // CLAUDE.md §7: saída exata calculada a partir da fórmula
        // documentada em IndiceDeSorte.kt — nenhuma aleatoriedade envolvida.
        assertEquals(18, indiceDeSorteDoDia("Marlene", LocalDate.of(2026, 8, 17)))
    }

    @Test
    fun indiceSempreEntre0E99IncluindoSemNome() {
        val nomes = listOf(null, "", "Marlene", "José", "A")

        (1..28).forEach { dia ->
            nomes.forEach { nome ->
                val indice = indiceDeSorteDoDia(nome, LocalDate.of(2026, 1, dia))
                assertTrue("indice era $indice para nome=$nome dia=$dia", indice in 0..99)
            }
        }
    }

    @Test
    fun diasDiferentesTendemAProduzirIndicesDiferentes() {
        val indice1 = indiceDeSorteDoDia("Marlene", LocalDate.of(2026, 8, 17))
        val indice2 = indiceDeSorteDoDia("Marlene", LocalDate.of(2026, 8, 18))

        assertNotEquals(indice1, indice2)
    }

    @Test
    fun nomesDiferentesTendemAProduzirIndicesDiferentesNoMesmoDia() {
        val data = LocalDate.of(2026, 8, 17)

        val indice1 = indiceDeSorteDoDia("Marlene", data)
        val indice2 = indiceDeSorteDoDia("José", data)

        assertNotEquals(indice1, indice2)
    }
}
