package com.trevo.core.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

// RNF-03.2 — trava a regressão do achado de contraste (2026-08-23): telas que
// escrevem texto sobre NocturneAccent sólido (GradeDeDezenas.CelulaDaGrade,
// TelaConferencia.LinhaDeDezenas, TelaPaywall.SelinhoDoPaywall) usam
// NocturneBackground, não NocturneText — a combinação errada dava ~2,66:1.
class ContrasteDasCoresTest {
    @Test
    fun `NocturneBackground sobre NocturneAccent atinge o minimo de 4,5 para 1`() {
        val razao = razaoDeContraste(NocturneBackground, NocturneAccent)
        assertTrue("Razão de contraste era $razao, esperado >= 4.5", razao >= 4.5)
    }

    @Test
    fun `NocturneText sobre NocturneAccent nao atinge nem o minimo de UI de 3 para 1`() {
        // Documenta por que a combinação antiga estava errada — se essa
        // asserção um dia falhar, é porque as cores da paleta mudaram e o
        // teste acima precisa ser revisado também.
        val razao = razaoDeContraste(NocturneText, NocturneAccent)
        assertTrue("Razão de contraste era $razao, esperado < 3.0", razao < 3.0)
    }
}

private fun razaoDeContraste(
    a: Color,
    b: Color,
): Double {
    val luminanciaA = luminanciaRelativa(a)
    val luminanciaB = luminanciaRelativa(b)
    val maior = maxOf(luminanciaA, luminanciaB)
    val menor = minOf(luminanciaA, luminanciaB)
    return (maior + 0.05) / (menor + 0.05)
}

// Fórmula de luminância relativa do WCAG 2.x.
private fun luminanciaRelativa(cor: Color): Double {
    fun canal(valor: Float): Double {
        val c = valor.toDouble()
        return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * canal(cor.red) + 0.7152 * canal(cor.green) + 0.0722 * canal(cor.blue)
}
