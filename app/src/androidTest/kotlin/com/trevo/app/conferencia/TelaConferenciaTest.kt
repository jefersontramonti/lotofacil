package com.trevo.app.conferencia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.app.detalhe.tagDezenaNaGrade
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

/**
 * RF-05 — Conferência. Wireframes 1j (resultado saiu) e 1k (offline e erro).
 */
class TelaConferenciaTest {
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
        uiState: ConferenciaUiState,
        onVoltarClick: () -> Unit = {},
        onTentarNovamenteClick: () -> Unit = {},
        onInformarResultadoManualmente: (Set<Int>) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaConferencia(
                    uiState = uiState,
                    onVoltarClick = onVoltarClick,
                    onTentarNovamenteClick = onTentarNovamenteClick,
                    onInformarResultadoManualmente = onInformarResultadoManualmente,
                )
            }
        }
    }

    @Test
    fun estadoDeCarregandoMostraTextoDeCarregando() {
        mostrarTela(ConferenciaUiState.Carregando)

        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_carregando)).assertIsDisplayed()
    }

    @Test
    fun estadoDeEsperaMostraTitulo() {
        mostrarTela(ConferenciaUiState.Espera)

        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_espera_titulo)).assertIsDisplayed()
    }

    @Test
    fun estadoSemConexaoMostraTituloEBotaoDeTentarDeNovoDisparaCallback() {
        var tentou = false
        mostrarTela(ConferenciaUiState.SemConexao, onTentarNovamenteClick = { tentou = true })

        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_sem_conexao_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTAO_TENTAR_NOVAMENTE).performClick()

        assertTrue(tentou)
    }

    @Test
    fun estadoDeFalhaMostraTituloEBotaoDeTentarDeNovoDisparaCallback() {
        var tentou = false
        mostrarTela(ConferenciaUiState.Falha, onTentarNovamenteClick = { tentou = true })

        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_falha_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTAO_TENTAR_NOVAMENTE).performClick()

        assertTrue(tentou)
    }

    private val sucessoDeExemplo =
        ConferenciaUiState.Sucesso(
            numeroDoConcurso = 3457,
            dezenasSorteadas = (1..15).toList(),
            totalGanho = BigDecimal("30.00"),
            totalGasto = BigDecimal("6.00"),
            itens =
                listOf(
                    PalpiteConferidoUiState(
                        numeroDoDia = 1,
                        dezenas = (1..15).toList(),
                        dezenasAcertadas = (1..13).toSet(),
                        acertos = 13,
                        premio = BigDecimal("30.00"),
                    ),
                    PalpiteConferidoUiState(
                        numeroDoDia = 2,
                        dezenas = listOf(3, 6, 8, 9, 11),
                        dezenasAcertadas = setOf(3, 8, 11),
                        acertos = 3,
                        premio = null,
                    ),
                ),
            origemManual = false,
        )

    @Test
    fun estadoDeSucessoMostraConcursoTotaisECartoesPorPalpite() {
        mostrarTela(sucessoDeExemplo)

        composeTestRule
            .onNodeWithText(context.getString(R.string.conferencia_concurso_titulo, 3457))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.home_palpite_rotulo, 1)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.home_palpite_rotulo, 2)).assertIsDisplayed()
    }

    @Test
    fun estadoDeSucessoSemPalpitesMostraEstadoVazio() {
        mostrarTela(sucessoDeExemplo.copy(itens = emptyList()))

        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_vazio_titulo)).assertIsDisplayed()
    }

    @Test
    fun resultadoManualNaoMostraNumeroDoConcurso() {
        mostrarTela(sucessoDeExemplo.copy(numeroDoConcurso = null, origemManual = true))

        composeTestRule
            .onNodeWithText(context.getString(R.string.conferencia_resultado_manual_titulo))
            .assertIsDisplayed()
    }

    @Test
    fun botaoVoltarDisparaCallback() {
        var voltou = false
        mostrarTela(ConferenciaUiState.Espera, onVoltarClick = { voltou = true })

        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.conferencia_voltar_descricao),
            ).performClick()

        assertTrue(voltou)
    }

    @Test
    fun informarResultadoManualmenteComQuinzeDezenasDisparaCallbackComAsDezenasEscolhidas() {
        var dezenasInformadas: Set<Int>? = null
        mostrarTela(ConferenciaUiState.Falha, onInformarResultadoManualmente = { dezenasInformadas = it })

        composeTestRule.onNodeWithTag(TAG_BOTAO_INFORMAR_MANUALMENTE).performClick()
        (1..15).forEach { dezena -> composeTestRule.onNodeWithTag(tagDezenaNaGrade(dezena)).performClick() }
        composeTestRule.onNodeWithTag(TAG_BOTAO_CONFIRMAR_MANUAL).performClick()

        assertEquals((1..15).toSet(), dezenasInformadas)
    }

    @Test
    fun nenhumaStringDaTelaDeConferenciaPrometeAumentoDeChance() {
        val stringsDaTela =
            buildMap {
                put("conferencia_carregando", context.getString(R.string.conferencia_carregando))
                put("conferencia_espera_descricao", context.getString(R.string.conferencia_espera_descricao))
                put("conferencia_sem_conexao_descricao", context.getString(R.string.conferencia_sem_conexao_descricao))
                put("conferencia_falha_descricao", context.getString(R.string.conferencia_falha_descricao))
                put("conferencia_disclaimer_oficial", context.getString(R.string.conferencia_disclaimer_oficial))
                put("conferencia_legenda_bolas", context.getString(R.string.conferencia_legenda_bolas))
                put("conferencia_vazio_descricao", context.getString(R.string.conferencia_vazio_descricao))
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
