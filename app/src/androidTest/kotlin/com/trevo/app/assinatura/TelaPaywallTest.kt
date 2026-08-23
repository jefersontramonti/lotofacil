package com.trevo.app.assinatura

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.data.assinatura.PRODUTO_ID_ANUAL
import com.trevo.core.data.assinatura.PRODUTO_ID_MENSAL
import com.trevo.core.data.assinatura.ProdutoDeAssinatura
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** RF-09.4/09.5 — Paywall. Wireframe 1n. */
class TelaPaywallTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val produtoAnual = ProdutoDeAssinatura(PRODUTO_ID_ANUAL, "R$ 89,90", "oferta-anual")
    private val produtoMensal = ProdutoDeAssinatura(PRODUTO_ID_MENSAL, "R$ 11,90", "oferta-mensal")

    private val estadoComProdutos =
        PaywallUiState(
            carregando = false,
            produtos = listOf(produtoAnual, produtoMensal),
            produtoSelecionadoId = PRODUTO_ID_ANUAL,
        )

    private fun mostrarTelaPaywall(
        uiState: PaywallUiState = estadoComProdutos,
        onFecharClick: () -> Unit = {},
        onEscolherPlanoClick: (String) -> Unit = {},
        onComecarTesteClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaPaywall(
                    uiState = uiState,
                    onFecharClick = onFecharClick,
                    onEscolherPlanoClick = onEscolherPlanoClick,
                    onComecarTesteClick = onComecarTesteClick,
                )
            }
        }
    }

    @Test
    fun exibeOsDoisPlanosComOPrecoReal() {
        mostrarTelaPaywall()

        composeTestRule.onNodeWithTag(tagPaywallPlano(PRODUTO_ID_ANUAL)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(tagPaywallPlano(PRODUTO_ID_MENSAL)).assertIsDisplayed()
        composeTestRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeTestRule.onNodeWithText("R$ 11,90").assertIsDisplayed()
    }

    @Test
    fun semProdutosDisponiveisNoPlayConsoleExibeIndisponivelSemInventarPreco() {
        mostrarTelaPaywall(uiState = PaywallUiState(carregando = false, produtos = emptyList()))

        composeTestRule.onNodeWithText(context.getString(R.string.paywall_indisponivel)).assertIsDisplayed()
    }

    @Test
    fun semProdutosDisponiveisOValorDoProContinuaVisivel() {
        mostrarTelaPaywall(uiState = PaywallUiState(carregando = false, produtos = emptyList()))

        composeTestRule.onNodeWithText(context.getString(R.string.paywall_titulo)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.paywall_item_palpites_nome))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.paywall_teste_titulo)).assertIsDisplayed()
    }

    @Test
    fun tocarUmPlanoDisparaOCallbackComOProductIdCorreto() {
        var escolhido: String? = null
        mostrarTelaPaywall(onEscolherPlanoClick = { escolhido = it })

        composeTestRule.onNodeWithTag(tagPaywallPlano(PRODUTO_ID_MENSAL)).performClick()

        assertTrue(escolhido == PRODUTO_ID_MENSAL)
    }

    @Test
    fun tocarComecarTesteDisparaOCallback() {
        var comecou = false
        mostrarTelaPaywall(onComecarTesteClick = { comecou = true })

        composeTestRule.onNodeWithTag(TAG_PAYWALL_CTA_COMECAR).performClick()

        assertTrue(comecou)
    }

    @Test
    fun tocarFecharOuContinuarNoGratisDisparaOCallback() {
        var fechou = false
        mostrarTelaPaywall(onFecharClick = { fechou = true })

        composeTestRule.onNodeWithTag(TAG_PAYWALL_CTA_CONTINUAR_GRATIS).performScrollTo().performClick()

        assertTrue(fechou)
    }

    // RNF-03.1 — ✕ do cabeçalho era um alvo de 32dp (achado de auditoria de
    // acessibilidade, 2026-08-23), corrigido envolvendo o glifo num Box de 48dp
    // (mesmo padrão de BotaoVoltar — minimumInteractiveComponentSize() não
    // funciona sobre um Text isolado, só Box/Row/Column respeitam o tamanho).
    @Test
    fun botaoFecharTemAlvoDeToqueDeAoMenos48dp() {
        mostrarTelaPaywall()

        composeTestRule
            .onNodeWithTag(TAG_PAYWALL_FECHAR)
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    // RNF-03.4 — o cartão de plano selecionado não expunha o estado
    // selecionado/não selecionado ao TalkBack (mesmo achado de auditoria).
    @Test
    fun cartaoDePlanoExpoeEstadoDeSelecaoAoTalkBack() {
        mostrarTelaPaywall()

        composeTestRule.onNodeWithTag(tagPaywallPlano(PRODUTO_ID_ANUAL)).assertIsSelected()
        composeTestRule.onNodeWithTag(tagPaywallPlano(PRODUTO_ID_MENSAL)).assertIsNotSelected()
    }
}
