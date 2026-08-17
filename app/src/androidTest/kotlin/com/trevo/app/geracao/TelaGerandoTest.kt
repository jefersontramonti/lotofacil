package com.trevo.app.geracao

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * RF-02.9 — "Exibir animação de ritual durante a geração, com no mínimo
 * três frases sequenciais." Wireframe 1f (Docs/Trevo - Wireframes.dc.html):
 * ícone, frase em negrito que muda, subtítulo fixo "montando seu volante".
 */
class TelaGerandoTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val frases =
        listOf(
            R.string.geracao_frase_1,
            R.string.geracao_frase_2,
            R.string.geracao_frase_3,
            R.string.geracao_frase_4,
        )

    private fun mostrarTelaGerando(indiceFrase: Int) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaGerando(
                    uiState = GeracaoUiState(indiceFrase = indiceFrase),
                    movimentoReduzido = true,
                )
            }
        }
    }

    @Test
    fun exibeAPrimeiraFraseQuandoOIndiceEZero() {
        mostrarTelaGerando(indiceFrase = 0)

        composeTestRule.onNodeWithText(context.getString(frases[0])).assertIsDisplayed()
    }

    @Test
    fun exibeASegundaFraseQuandoOIndiceEUm() {
        mostrarTelaGerando(indiceFrase = 1)

        composeTestRule.onNodeWithText(context.getString(frases[1])).assertIsDisplayed()
    }

    @Test
    fun exibeATerceiraFraseQuandoOIndiceEDois() {
        mostrarTelaGerando(indiceFrase = 2)

        composeTestRule.onNodeWithText(context.getString(frases[2])).assertIsDisplayed()
    }

    @Test
    fun exibeAQuartaFraseQuandoOIndiceETres() {
        mostrarTelaGerando(indiceFrase = 3)

        composeTestRule.onNodeWithText(context.getString(frases[3])).assertIsDisplayed()
    }

    @Test
    fun exibeOSubtituloFixoIndependenteDaFraseAtual() {
        mostrarTelaGerando(indiceFrase = 2)

        composeTestRule.onNodeWithText(context.getString(R.string.geracao_subtitulo)).assertIsDisplayed()
    }

    @Test
    fun nenhumaFraseOuSubtituloDoRitualPrometeAumentoDeChance() {
        val expressoesProibidas = listOf("aumenta", "garante", "mais chance", "melhora sua chance")
        val stringsDaTela =
            frases.mapIndexed { indice, id -> "geracao_frase_${indice + 1}" to context.getString(id) } +
                listOf("geracao_subtitulo" to context.getString(R.string.geracao_subtitulo))

        stringsDaTela.forEach { (nomeRecurso, valor) ->
            val valorEmMinusculas = valor.lowercase()
            expressoesProibidas.forEach { expressaoProibida ->
                assertTrue(
                    "$nomeRecurso não pode conter \"$expressaoProibida\", mas era: \"$valor\"",
                    !valorEmMinusculas.contains(expressaoProibida),
                )
            }
        }
    }
}
