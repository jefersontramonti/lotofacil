package com.trevo.app.historico

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * RF-06 — Histórico. Wireframe 1l.
 */
class TelaHistoricoTest {
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

    private fun mostrarTela(
        uiState: HistoricoUiState,
        onVerMaisClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaHistorico(uiState = uiState, onVerMaisClick = onVerMaisClick)
            }
        }
    }

    @Test
    fun estadoVazioMostraTituloEDescricao() {
        mostrarTela(HistoricoUiState.Vazio)

        composeTestRule.onNodeWithText(context.getString(R.string.historico_vazio_titulo)).assertIsDisplayed()
    }

    private val concursoDeExemplo =
        ConcursoConferidoUiState(
            numero = 3457,
            data = LocalDate.of(2026, 8, 17),
            premioTotal = BigDecimal("30.00"),
            palpites =
                listOf(
                    PalpiteNoHistoricoUiState(
                        numeroDoDia = 1,
                        dezenas = (1..15).toList(),
                        acertos = 13,
                        premio = BigDecimal("30.00"),
                    ),
                ),
        )

    private val estadoComDados =
        HistoricoUiState.ComDados(
            totalDeJogos = 1,
            totalDeConcursos = 1,
            totalGasto = BigDecimal("3.50"),
            totalGanho = BigDecimal("30.00"),
            saldo = BigDecimal("26.50"),
            retornoPercentual = 857,
            mediaGastoPorConcurso = BigDecimal("3.50"),
            melhorResultadoEmAcertos = 13,
            faixas = (15 downTo 11).map { FaixaHistoricoUiState(it, if (it == 13) 1 else 0) },
            concursosRevelados = listOf(concursoDeExemplo),
            temMaisConcursos = false,
            quantidadeDeConcursosRestantes = 0,
        )

    @Test
    fun estadoComDadosMostraTotaisEConcurso() {
        mostrarTela(estadoComDados)

        composeTestRule.onNodeWithText(context.getString(R.string.historico_gastou_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.historico_ganhou_titulo)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.home_palpite_rotulo, 1))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun botaoVerMaisApareceEDisparaCallback() {
        var tocou = false
        mostrarTela(
            estadoComDados.copy(temMaisConcursos = true, quantidadeDeConcursosRestantes = 2),
            onVerMaisClick = { tocou = true },
        )

        composeTestRule.onNodeWithTag(TAG_BOTAO_VER_MAIS_HISTORICO).performScrollTo().performClick()

        assertTrue(tocou)
    }

    @Test
    fun semConcursosRestantesNaoMostraBotaoVerMais() {
        mostrarTela(estadoComDados)

        composeTestRule.onAllNodesWithTag(TAG_BOTAO_VER_MAIS_HISTORICO).assertCountEquals(0)
    }

    @Test
    fun nenhumaStringDaTelaDeHistoricoPrometeAumentoDeChance() {
        val stringsDaTela =
            buildMap {
                put("historico_vazio_descricao", context.getString(R.string.historico_vazio_descricao))
                put(
                    "historico_retorno",
                    context.getString(R.string.historico_retorno, 100),
                )
                put(
                    "historico_media",
                    context.getString(R.string.historico_media, "R$ 3,50"),
                )
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
