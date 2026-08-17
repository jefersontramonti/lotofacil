package com.trevo.app.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * RF-03 (núcleo) — Home. Wireframes 1d ("Home · lista de palpites") e 1e
 * ("Home · limite atingido", reaproveitado aqui só pro estado vazio, já que
 * o anúncio recompensado é RF-09/monetização e não está nesta fatia).
 */
class TelaHomeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val expressoesProibidas =
        listOf(
            "aumenta",
            "garante",
            "mais chance",
            "melhora sua chance",
        )

    private val palpiteDeExemplo =
        PalpiteItemUiState(
            id = 1L,
            numeroDoDia = 1,
            dezenas = (1..15).toList(),
            forca = 80,
            horario = "09:41",
        )

    private fun mostrarTelaHome(
        uiState: HomeUiState = HomeUiState(carregando = false),
        onExcluirClick: (Long) -> Unit = {},
        onConfirmarExclusaoClick: () -> Unit = {},
        onCancelarExclusaoClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaHome(
                    uiState = uiState,
                    onExcluirClick = onExcluirClick,
                    onConfirmarExclusaoClick = onConfirmarExclusaoClick,
                    onCancelarExclusaoClick = onCancelarExclusaoClick,
                )
            }
        }
    }

    @Test
    fun semPalpitesExibeOEstadoVazio() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, palpitesHoje = emptyList()))

        composeTestRule.onNodeWithText(context.getString(R.string.home_vazio_titulo)).assertIsDisplayed()
    }

    @Test
    fun comUmPalpiteExibeODezenasHorarioEForca() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, palpitesHoje = listOf(palpiteDeExemplo)))

        composeTestRule.onNodeWithTag(tagCartaoPalpite(palpiteDeExemplo.id)).assertIsDisplayed()
        composeTestRule.onNodeWithText("01").assertIsDisplayed()
        composeTestRule.onNodeWithText("15").assertIsDisplayed()
        composeTestRule.onNodeWithText(palpiteDeExemplo.horario).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.home_palpite_forca, palpiteDeExemplo.forca))
            .assertIsDisplayed()
    }

    @Test
    fun tocarExcluirDisparaOnExcluirClickComOIdCorreto() {
        var idExcluido: Long? = null

        mostrarTelaHome(
            uiState = HomeUiState(carregando = false, palpitesHoje = listOf(palpiteDeExemplo)),
            onExcluirClick = { idExcluido = it },
        )

        composeTestRule.onNodeWithTag(tagBotaoExcluirPalpite(palpiteDeExemplo.id)).performClick()

        assertEquals(palpiteDeExemplo.id, idExcluido)
    }

    @Test
    fun comPedidoDeExclusaoPendenteExibeODialogoDeConfirmacao() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    palpitesHoje = listOf(palpiteDeExemplo),
                    palpiteParaConfirmarExclusao = palpiteDeExemplo.id,
                ),
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.home_excluir_confirmar_titulo))
            .assertIsDisplayed()
    }

    @Test
    fun confirmarNoDialogoDisparaOnConfirmarExclusaoClick() {
        var confirmado = false

        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    palpitesHoje = listOf(palpiteDeExemplo),
                    palpiteParaConfirmarExclusao = palpiteDeExemplo.id,
                ),
            onConfirmarExclusaoClick = { confirmado = true },
        )

        composeTestRule.onNodeWithText(context.getString(R.string.home_excluir_confirmar_cta)).performClick()

        assertTrue(confirmado)
    }

    @Test
    fun cancelarNoDialogoDisparaOnCancelarExclusaoClickENaoOOutroCallback() {
        var confirmado = false
        var cancelado = false

        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    palpitesHoje = listOf(palpiteDeExemplo),
                    palpiteParaConfirmarExclusao = palpiteDeExemplo.id,
                ),
            onConfirmarExclusaoClick = { confirmado = true },
            onCancelarExclusaoClick = { cancelado = true },
        )

        composeTestRule.onNodeWithText(context.getString(R.string.home_excluir_cancelar_cta)).performClick()

        assertTrue(cancelado)
        assertFalse(confirmado)
    }

    @Test
    fun exibeODisclaimerDeQueAApostaEFeitaPeloUsuarioNaLotericaOuCaixa() {
        mostrarTelaHome()

        composeTestRule.onNodeWithText(context.getString(R.string.home_disclaimer_aposta)).assertIsDisplayed()
    }

    @Test
    fun nenhumaStringDaTelaDeHomePrometeAumentoDeChance() {
        val stringsDaTela =
            buildMap {
                put("home_horario_apostas", context.getString(R.string.home_horario_apostas))
                put("home_secao_palpites_titulo", context.getString(R.string.home_secao_palpites_titulo))
                put("home_vazio_titulo", context.getString(R.string.home_vazio_titulo))
                put("home_vazio_descricao", context.getString(R.string.home_vazio_descricao))
                put("home_disclaimer_aposta", context.getString(R.string.home_disclaimer_aposta))
            }

        stringsDaTela.forEach { (nomeRecurso, valor) ->
            val valorEmMinusculas = valor.lowercase()
            expressoesProibidas.forEach { expressaoProibida ->
                assertTrue(
                    "$nomeRecurso não pode conter \"$expressaoProibida\" (promessa de aumento de chance), " +
                        "mas era: \"$valor\"",
                    !valorEmMinusculas.contains(expressaoProibida),
                )
            }
        }
    }
}
