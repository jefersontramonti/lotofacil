package com.trevo.app.conferencia

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.app.detalhe.tagDezenaNaGrade
import com.trevo.core.engine.resultado.FaixaDePremio
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

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
            dataApuracao = LocalDate.of(2026, 8, 21),
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
            faixasDePremio =
                listOf(
                    FaixaDePremio(acertosNecessarios = 15, numeroDeGanhadores = 0, valorPremio = BigDecimal.ZERO),
                    FaixaDePremio(
                        acertosNecessarios = 14,
                        numeroDeGanhadores = 207,
                        valorPremio = BigDecimal("2251.87"),
                    ),
                    FaixaDePremio(
                        acertosNecessarios = 13,
                        numeroDeGanhadores = 6937,
                        valorPremio = BigDecimal("35.00"),
                    ),
                ),
            acumulado = true,
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
    fun estadoDeSucessoMostraDataDoSorteioETabelaDePremiacao() {
        mostrarTela(sucessoDeExemplo)

        composeTestRule.onNodeWithText("21/08/2026").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_premiacao_titulo)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_acumulado)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.conferencia_premiacao_sem_ganhador))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                context.resources.getQuantityString(
                    R.plurals.conferencia_premiacao_ganhadores,
                    207,
                    207,
                    formatarReaisDeTeste(BigDecimal("2251.87")),
                ),
            ).assertIsDisplayed()
    }

    @Test
    fun resultadoManualNaoMostraDataNemTabelaDePremiacao() {
        mostrarTela(sucessoDeExemplo.copy(origemManual = true, faixasDePremio = emptyList()))

        composeTestRule.onNodeWithText("21/08/2026").assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.conferencia_premiacao_titulo)).assertDoesNotExist()
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

    // RNF-03.1 — "Informar resultado manualmente"/"Cancelar"/"Confirmar"
    // eram alvos de toque sem tamanho garantido (achado de auditoria de
    // acessibilidade, 2026-08-23), corrigidos envolvendo o texto num
    // Box(height = 48.dp) — Text isolado ignora constraints de tamanho
    // mínimo, só Box/Row/Column respeitam.
    @Test
    fun botaoInformarManualmenteTemAlvoDeToqueDeAoMenos48dp() {
        mostrarTela(ConferenciaUiState.Falha)

        composeTestRule
            .onNodeWithTag(TAG_BOTAO_INFORMAR_MANUALMENTE)
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun botaoConfirmarManualTemAlvoDeToqueDeAoMenos48dp() {
        mostrarTela(ConferenciaUiState.Falha)
        composeTestRule.onNodeWithTag(TAG_BOTAO_INFORMAR_MANUALMENTE).performClick()
        (1..15).forEach { dezena -> composeTestRule.onNodeWithTag(tagDezenaNaGrade(dezena)).performClick() }

        composeTestRule
            .onNodeWithTag(TAG_BOTAO_CONFIRMAR_MANUAL)
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
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
                put("conferencia_acumulado", context.getString(R.string.conferencia_acumulado))
                put("conferencia_premiacao_titulo", context.getString(R.string.conferencia_premiacao_titulo))
                put(
                    "conferencia_premiacao_sem_ganhador",
                    context.getString(R.string.conferencia_premiacao_sem_ganhador),
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

private fun formatarReaisDeTeste(valor: BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
