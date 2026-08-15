package com.trevo.app.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * RF-01.7 — "Apresentar as 12 crenças com nome e descrição curta,
 * permitindo seleção múltipla."
 *
 * O limite de 3 no plano grátis e o cadeado nas demais (RF-01.8) ficam
 * fora de escopo aqui — nenhuma crença é bloqueada nesta tela.
 */
class TelaCrencasTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val titulo get() = context.getString(R.string.crencas_titulo)
    private val subtitulo get() = context.getString(R.string.crencas_subtitulo)
    private val ctaVoltar get() = context.getString(R.string.crencas_cta_voltar)
    private val ctaContinuar get() = context.getString(R.string.crencas_cta_continuar)

    private val expressoesProibidas =
        listOf(
            "aumenta",
            "garante",
            "mais chance",
            "melhora sua chance",
        )

    private val nomeStringDe =
        mapOf(
            Crenca.SIGNO to R.string.crenca_signo_nome,
            Crenca.NASCIMENTO to R.string.crenca_nascimento_nome,
            Crenca.QUENTES to R.string.crenca_quentes_nome,
            Crenca.ATRASADOS to R.string.crenca_atrasados_nome,
            Crenca.LUA to R.string.crenca_lua_nome,
            Crenca.SONHO to R.string.crenca_sonho_nome,
            Crenca.MOLDURA to R.string.crenca_moldura_nome,
            Crenca.PARES to R.string.crenca_pares_nome,
            Crenca.PRIMOS to R.string.crenca_primos_nome,
            Crenca.SOMA to R.string.crenca_soma_nome,
            Crenca.REPETIDAS to R.string.crenca_repetidas_nome,
            Crenca.NUMEROLOGIA to R.string.crenca_numerologia_nome,
        )

    private val descricaoStringDe =
        mapOf(
            Crenca.SIGNO to R.string.crenca_signo_desc,
            Crenca.NASCIMENTO to R.string.crenca_nascimento_desc,
            Crenca.QUENTES to R.string.crenca_quentes_desc,
            Crenca.ATRASADOS to R.string.crenca_atrasados_desc,
            Crenca.LUA to R.string.crenca_lua_desc,
            Crenca.SONHO to R.string.crenca_sonho_desc,
            Crenca.MOLDURA to R.string.crenca_moldura_desc,
            Crenca.PARES to R.string.crenca_pares_desc,
            Crenca.PRIMOS to R.string.crenca_primos_desc,
            Crenca.SOMA to R.string.crenca_soma_desc,
            Crenca.REPETIDAS to R.string.crenca_repetidas_desc,
            Crenca.NUMEROLOGIA to R.string.crenca_numerologia_desc,
        )

    private fun mostrarTelaCrencas(
        uiState: CrencasUiState = CrencasUiState(),
        onCrencaClick: (Crenca) -> Unit = {},
        onVoltarClick: () -> Unit = {},
        onContinuarClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaCrencas(
                    uiState = uiState,
                    onCrencaClick = onCrencaClick,
                    onVoltarClick = onVoltarClick,
                    onContinuarClick = onContinuarClick,
                )
            }
        }
    }

    @Test
    fun exibeTituloESubtituloDaTelaDeCrencas() {
        mostrarTelaCrencas()

        composeTestRule.onNodeWithText(titulo).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitulo).assertIsDisplayed()
    }

    @Test
    fun exibeAsDozeCrencasComNomeEDescricaoCurta() {
        mostrarTelaCrencas()

        Crenca.entries.forEach { crenca ->
            composeTestRule.onNodeWithTag(tagCartaoCrenca(crenca)).performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText(context.getString(nomeStringDe.getValue(crenca))).assertIsDisplayed()
            composeTestRule
                .onNodeWithText(context.getString(descricaoStringDe.getValue(crenca)))
                .assertIsDisplayed()
        }
    }

    @Test
    fun nenhumaCrencaComecaSelecionadaQuandoOEstadoInicialEVazio() {
        mostrarTelaCrencas(uiState = CrencasUiState())

        Crenca.entries.forEach { crenca ->
            composeTestRule.onNodeWithTag(tagCartaoCrenca(crenca)).performScrollTo().assertIsOff()
        }
    }

    @Test
    fun crencasJaSelecionadasNoEstadoInicialAparecemMarcadas() {
        mostrarTelaCrencas(uiState = CrencasUiState(selecionadas = setOf(Crenca.SIGNO, Crenca.LUA)))

        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.SIGNO)).performScrollTo().assertIsOn()
        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.LUA)).performScrollTo().assertIsOn()
        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.SONHO)).performScrollTo().assertIsOff()
    }

    @Test
    fun tocarUmCartaoDeCrencaDisparaOnCrencaClickComACrencaCorreta() {
        var crencaTocada: Crenca? = null

        mostrarTelaCrencas(onCrencaClick = { crencaTocada = it })

        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.QUENTES)).performScrollTo().performClick()

        assertEquals(Crenca.QUENTES, crencaTocada)
    }

    @Test
    fun tocarUmCartaoAlternaOEstadoDeMarcacaoVisivelDaTela() {
        var uiState by mutableStateOf(CrencasUiState())

        composeTestRule.setContent {
            TrevoTheme {
                TelaCrencas(
                    uiState = uiState,
                    onCrencaClick = { crenca ->
                        val atual = uiState.selecionadas
                        uiState = uiState.copy(selecionadas = if (crenca in atual) atual - crenca else atual + crenca)
                    },
                    onVoltarClick = {},
                    onContinuarClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.MOLDURA)).performScrollTo().assertIsOff()
        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.MOLDURA)).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.MOLDURA)).assertIsOn()
    }

    @Test
    fun botoesVoltarEContinuarExistemEDisparamOsCallbacksCorrespondentes() {
        var voltouClicado = false
        var continuouClicado = false

        mostrarTelaCrencas(
            onVoltarClick = { voltouClicado = true },
            onContinuarClick = { continuouClicado = true },
        )

        composeTestRule.onNodeWithText(ctaVoltar).performScrollTo().performClick()
        assertTrue(voltouClicado)

        composeTestRule.onNodeWithText(ctaContinuar).performScrollTo().performClick()
        assertTrue(continuouClicado)
    }

    @Test
    fun nenhumaStringDaTelaDeCrencasPrometeAumentoDeChance() {
        val stringsDaTela =
            buildMap {
                put("crencas_titulo", titulo)
                put("crencas_subtitulo", subtitulo)
                put("crencas_cta_voltar", ctaVoltar)
                put("crencas_cta_continuar", ctaContinuar)
                Crenca.entries.forEach { crenca ->
                    put("nome de $crenca", context.getString(nomeStringDe.getValue(crenca)))
                    put("descrição de $crenca", context.getString(descricaoStringDe.getValue(crenca)))
                }
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
