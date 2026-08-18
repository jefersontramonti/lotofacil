package com.trevo.app.detalhe

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

/** RF-04.9/04.10 — Desdobramentos. Wireframe 1i. */
class TelaDesdobramentosTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val estadoDeExemplo =
        DesdobramentosUiState(
            carregando = false,
            quantidadeDeDezenas = 18,
            jogosEquivalentes = 816,
            custoTotal = BigDecimal("2856.00"),
            combinacoesExibidas = (1..18).toList().let { dezenas -> listOf(dezenas.take(15), dezenas.takeLast(15)) },
        )

    private fun mostrarTelaDesdobramentos(
        uiState: DesdobramentosUiState = estadoDeExemplo,
        onVoltarClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaDesdobramentos(uiState = uiState, onVoltarClick = onVoltarClick)
            }
        }
    }

    @Test
    fun exibeAExplicacaoComAQuantidadeDeDezenas() {
        mostrarTelaDesdobramentos()

        composeTestRule
            .onNodeWithText(context.getString(R.string.desdobramentos_explicacao, 18))
            .assertIsDisplayed()
    }

    @Test
    fun exibeAQuantidadeDeJogosEOCustoTotal() {
        mostrarTelaDesdobramentos()

        composeTestRule.onNodeWithText("816").assertIsDisplayed()
    }

    @Test
    fun exibeANotaDeQuantasCombinacoesEstaoSendoMostradas() {
        mostrarTelaDesdobramentos()

        composeTestRule.onNodeWithText(context.getString(R.string.desdobramentos_mostrando, 2, 816)).assertIsDisplayed()
    }

    @Test
    fun tocarVoltarDisparaOCallback() {
        var voltou = false

        mostrarTelaDesdobramentos(onVoltarClick = { voltou = true })

        composeTestRule.onNodeWithText("←").performClick()

        assertTrue(voltou)
    }
}
