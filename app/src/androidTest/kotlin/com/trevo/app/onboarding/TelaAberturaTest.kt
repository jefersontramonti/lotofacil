package com.trevo.app.onboarding

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.ui.TrevoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * RF-01.1 — "Apresentar tela inicial com a proposta do app e o aviso de
 * que o sorteio é aleatório, antes de qualquer cadastro."
 *
 * Wireframe 1a (Onboarding · abertura), bloco `onb0` do protótipo.
 *
 * [TelaAbertura] ainda não existe em produção: este arquivo deve falhar
 * a compilação até que o `trevo-developer` a implemente, junto com
 * `BotaoPrimario` (:core:ui) e as strings `abertura_*` (app/strings.xml).
 */
@RunWith(AndroidJUnit4::class)
class TelaAberturaTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val titulo get() = context.getString(R.string.app_name)
    private val proposta get() = context.getString(R.string.abertura_proposta)
    private val avisoAleatoriedade get() = context.getString(R.string.abertura_aviso_aleatoriedade)
    private val ctaComecar get() = context.getString(R.string.abertura_cta_comecar)

    // Lista negativa: nenhum texto da tela pode insinuar que alguma
    // crença/método aumenta a chance de acerto — CLAUDE.md, regra
    // inviolável 2.
    private val expressoesProibidas =
        listOf(
            "aumenta",
            "garante",
            "mais chance",
            "melhora sua chance",
        )

    private fun mostrarTelaAbertura(onComecarClick: () -> Unit = {}) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaAbertura(onComecarClick = onComecarClick)
            }
        }
    }

    @Test
    fun exibeTituloPropostaEAvisoDeAleatoriedadeSimultaneamenteSemInteracao() {
        mostrarTelaAbertura()

        composeTestRule.onNodeWithText(titulo).assertIsDisplayed()
        composeTestRule.onNodeWithText(proposta).assertIsDisplayed()
        composeTestRule.onNodeWithText(avisoAleatoriedade).assertIsDisplayed()
    }

    @Test
    fun avisoDeAleatoriedadeContemAsExpressoesDeConformidadeObrigatorias() {
        // Guardrail: RF-01.1 exige o aviso de que o sorteio é aleatório;
        // CLAUDE.md regra 2 exige negar explicitamente ganho de chance.
        assert(avisoAleatoriedade.contains("aleatório")) {
            "abertura_aviso_aleatoriedade deve conter a palavra 'aleatório', mas era: \"$avisoAleatoriedade\""
        }
        assert(avisoAleatoriedade.contains("Nenhuma crença muda a probabilidade")) {
            "abertura_aviso_aleatoriedade deve conter a frase 'Nenhuma crença muda a probabilidade', " +
                "mas era: \"$avisoAleatoriedade\""
        }
    }

    @Test
    fun nenhumaStringDaTelaDeAberturaPrometeAumentoDeChance() {
        val stringsDaTela =
            mapOf(
                "abertura_proposta" to proposta,
                "abertura_aviso_aleatoriedade" to avisoAleatoriedade,
                "abertura_cta_comecar" to ctaComecar,
            )

        stringsDaTela.forEach { (nomeRecurso, valor) ->
            val valorEmMinusculas = valor.lowercase()
            expressoesProibidas.forEach { expressaoProibida ->
                assert(!valorEmMinusculas.contains(expressaoProibida)) {
                    "$nomeRecurso não pode conter \"$expressaoProibida\" (promessa de aumento de chance), " +
                        "mas era: \"$valor\""
                }
            }
        }
    }

    @Test
    fun cliqueNoBotaoComecarDisparaCallbackExatamenteUmaVez() {
        var contadorDeCliques = 0
        mostrarTelaAbertura(onComecarClick = { contadorDeCliques++ })

        composeTestRule.onNodeWithText(ctaComecar).performClick()

        assert(contadorDeCliques == 1) {
            "onComecarClick deveria disparar exatamente 1 vez após um clique, mas disparou $contadorDeCliques vez(es)"
        }
    }

    @Test
    fun botaoComecarTemAlvoDeToqueDeAoMenos48dp() {
        // RNF-03.1 — alvo de toque mínimo de 48dp (CLAUDE.md seção 6).
        mostrarTelaAbertura()

        composeTestRule
            .onNodeWithText(ctaComecar)
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun conteudoPermaneceAcessivelComFonteDoSistemaA200PorCento() {
        // RNF-03.3 — suporte a fonte do sistema até 200% sem corte
        // (CLAUDE.md seção 6).
        composeTestRule.setContent {
            val densidadeOriginal = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = densidadeOriginal.density, fontScale = 2f),
            ) {
                TrevoTheme {
                    TelaAbertura(onComecarClick = {})
                }
            }
        }

        composeTestRule.onNodeWithText(titulo, substring = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(avisoAleatoriedade, substring = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(ctaComecar, substring = true).performScrollTo().assertIsDisplayed()
    }
}
