package com.trevo.app.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * RF-01.7 — "Apresentar as 12 crenças com nome e descrição curta,
 * permitindo seleção múltipla."
 * RF-01.8 — "Limitar a seleção a três crenças no plano grátis, exibindo
 * cadeado nas demais e conduzindo ao paywall ao tocá-las." Wireframe 1c
 * (Docs/Trevo - Wireframes.dc.html): título, subtítulo com a contagem em
 * negrito, cadeado nas crenças travadas e CTA "Entrar no app" — o palpite
 * gerado não é exibido nesta tela (fica pronto pra tela de destino que
 * RF-03/home ainda não define).
 */
class TelaCrencasTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val titulo get() = context.getString(R.string.crencas_titulo)
    private val ctaVoltar get() = context.getString(R.string.crencas_cta_voltar)
    private val ctaEntrarNoApp get() = context.getString(R.string.crencas_cta_entrar_no_app)

    private fun subtituloEsperado(quantidadeSelecionada: Int): String {
        val prefixo = context.getString(R.string.crencas_subtitulo_prefixo)
        val contagem =
            context.resources.getQuantityString(
                R.plurals.crencas_subtitulo_selecionadas,
                quantidadeSelecionada,
                quantidadeSelecionada,
            )
        return "$prefixo $contagem"
    }

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
        onCrencaBloqueadaClick: () -> Unit = {},
        onVoltarClick: () -> Unit = {},
        onContinuarClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaCrencas(
                    uiState = uiState,
                    onCrencaClick = onCrencaClick,
                    onCrencaBloqueadaClick = onCrencaBloqueadaClick,
                    onVoltarClick = onVoltarClick,
                    onContinuarClick = onContinuarClick,
                )
            }
        }
    }

    @Test
    fun exibeTituloESubtituloComContagemZeradaQuandoNadaEstaSelecionado() {
        mostrarTelaCrencas()

        composeTestRule.onNodeWithText(titulo).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtituloEsperado(0)).assertIsDisplayed()
    }

    @Test
    fun subtituloRefleteAContagemAtualDeCrencasSelecionadas() {
        mostrarTelaCrencas(uiState = CrencasUiState(selecionadas = setOf(Crenca.SIGNO, Crenca.LUA)))

        composeTestRule.onNodeWithText(subtituloEsperado(2)).assertIsDisplayed()
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
                    onCrencaBloqueadaClick = {},
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
    fun comTresCrencasSelecionadasAsDemaisExibemCadeadoEmVezDeCaixaDeMarcacao() {
        val tresSelecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO)

        mostrarTelaCrencas(uiState = CrencasUiState(selecionadas = tresSelecionadas))

        // A Row do cartão usa mergeDescendants — o cadeado só é alcançável
        // como nó próprio com useUnmergedTree = true, por uma tag dedicada
        // (tagCadeadoCrenca), não por texto: `onNodeWithText(cadeado)` sem
        // escopo casaria com um dos 9 cartões bloqueados ao mesmo tempo.
        composeTestRule
            .onNodeWithTag(tagCadeadoCrenca(Crenca.MOLDURA), useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.MOLDURA)).assertIsDisplayed()
    }

    @Test
    fun tocarUmaCrencaBloqueadaDisparaOnCrencaBloqueadaClickENaoOnCrencaClick() {
        var crencaTocada: Crenca? = null
        var bloqueadaTocada = false

        mostrarTelaCrencas(
            uiState = CrencasUiState(selecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO)),
            onCrencaClick = { crencaTocada = it },
            onCrencaBloqueadaClick = { bloqueadaTocada = true },
        )

        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.MOLDURA)).performScrollTo().performClick()

        assertTrue(bloqueadaTocada)
        assertNull(crencaTocada)
    }

    @Test
    fun crencaJaSelecionadaNuncaFicaBloqueadaMesmoNoLimiteDeTres() {
        val tresSelecionadas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO)
        var crencaTocada: Crenca? = null

        mostrarTelaCrencas(
            uiState = CrencasUiState(selecionadas = tresSelecionadas),
            onCrencaClick = { crencaTocada = it },
        )

        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.SIGNO)).performScrollTo().assertIsOn()
        composeTestRule.onNodeWithTag(tagCartaoCrenca(Crenca.SIGNO)).performClick()

        assertEquals(Crenca.SIGNO, crencaTocada)
    }

    @Test
    fun comMenosDeTresSelecionadasNenhumaCrencaFicaBloqueada() {
        mostrarTelaCrencas(uiState = CrencasUiState(selecionadas = setOf(Crenca.SIGNO, Crenca.LUA)))

        Crenca.entries.forEach { crenca ->
            composeTestRule.onNodeWithTag(tagCartaoCrenca(crenca)).performScrollTo().assertIsDisplayed()
        }
        // Nenhum cadeado na árvore inteira — todas as 12 continuam com caixa de marcação.
        Crenca.entries.forEach { crenca ->
            composeTestRule
                .onAllNodesWithTag(tagCadeadoCrenca(crenca), useUnmergedTree = true)
                .assertCountEquals(0)
        }
    }

    @Test
    fun botoesVoltarEEntrarNoAppExistemEDisparamOsCallbacksCorrespondentes() {
        var voltouClicado = false
        var continuouClicado = false

        mostrarTelaCrencas(
            onVoltarClick = { voltouClicado = true },
            onContinuarClick = { continuouClicado = true },
        )

        composeTestRule.onNodeWithText(ctaVoltar).performScrollTo().performClick()
        assertTrue(voltouClicado)

        composeTestRule.onNodeWithText(ctaEntrarNoApp).performScrollTo().performClick()
        assertTrue(continuouClicado)
    }

    @Test
    fun nenhumaStringDaTelaDeCrencasPrometeAumentoDeChance() {
        val stringsDaTela =
            buildMap {
                put("crencas_titulo", titulo)
                put("crencas_subtitulo", subtituloEsperado(0))
                put("crencas_cta_voltar", ctaVoltar)
                put("crencas_cta_entrar_no_app", ctaEntrarNoApp)
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
