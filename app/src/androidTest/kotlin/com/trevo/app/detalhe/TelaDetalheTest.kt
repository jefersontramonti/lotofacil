package com.trevo.app.detalhe

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
 * RF-04 — Detalhe do palpite. Wireframes 1g (visualização) e 1h (edição).
 */
class TelaDetalheTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val estadoDeExemplo =
        DetalheUiState(
            carregando = false,
            palpiteExiste = true,
            numeroDoDia = 2,
            dezenas = (1..15).toList(),
            dezenasFixas = listOf(1, 2),
            forca = 81,
            origens = listOf(OrigemDeDezenasUiState(Crenca.SIGNO, listOf(1, 2, 3))),
            soma = 120,
            pares = 7,
            impares = 8,
            moldura = 16,
            miolo = 9,
            custo = java.math.BigDecimal("3.50"),
            chanceRealUmEm = 3_268_760,
            quantidadeDeDezenas = 15,
        )

    private fun mostrarTelaDetalhe(
        uiState: DetalheUiState = estadoDeExemplo,
        onVoltarClick: () -> Unit = {},
        onEditarClick: () -> Unit = {},
        onRefazerClick: () -> Unit = {},
        onExcluirClick: () -> Unit = {},
        onConfirmarExclusaoClick: () -> Unit = {},
        onCancelarExclusaoClick: () -> Unit = {},
        onDezenaClick: (Int) -> Unit = {},
        onAlternarGuardarFixasClick: () -> Unit = {},
        onCancelarEdicaoClick: () -> Unit = {},
        onSalvarEdicaoClick: () -> Unit = {},
        onLimparFixasClick: () -> Unit = {},
        onVerDesdobramentosClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaDetalhe(
                    uiState = uiState,
                    onVoltarClick = onVoltarClick,
                    onEditarClick = onEditarClick,
                    onRefazerClick = onRefazerClick,
                    onExcluirClick = onExcluirClick,
                    onConfirmarExclusaoClick = onConfirmarExclusaoClick,
                    onCancelarExclusaoClick = onCancelarExclusaoClick,
                    onDezenaClick = onDezenaClick,
                    onAlternarGuardarFixasClick = onAlternarGuardarFixasClick,
                    onCancelarEdicaoClick = onCancelarEdicaoClick,
                    onSalvarEdicaoClick = onSalvarEdicaoClick,
                    onLimparFixasClick = onLimparFixasClick,
                    onVerDesdobramentosClick = onVerDesdobramentosClick,
                )
            }
        }
    }

    @Test
    fun exibeOTituloComONumeroDoDia() {
        mostrarTelaDetalhe()

        composeTestRule.onNodeWithText(context.getString(R.string.home_palpite_rotulo, 2)).assertIsDisplayed()
    }

    @Test
    fun exibeTodasAsDezenasMarcadasNaGrade() {
        mostrarTelaDetalhe()

        estadoDeExemplo.dezenas.forEach { dezena ->
            composeTestRule.onNodeWithTag(tagDezenaNaGrade(dezena)).assertIsDisplayed()
        }
    }

    @Test
    fun exibeForcaEChanceReal() {
        mostrarTelaDetalhe()

        composeTestRule.onNodeWithText("81").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                context.getString(R.string.detalhe_chance_valor, "3.268.760"),
            ).assertIsDisplayed()
    }

    @Test
    fun exibeAOrigemDeCadaCrencaComNomeDescricaoEDezenas() {
        mostrarTelaDetalhe()

        composeTestRule.onNodeWithText(context.getString(R.string.crenca_signo_nome)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.crenca_signo_desc)).assertIsDisplayed()
        composeTestRule.onNodeWithText("01 · 02 · 03").assertIsDisplayed()
    }

    @Test
    fun exibeEstatisticasDeSomaParesImparesMolduraMioloECusto() {
        mostrarTelaDetalhe()

        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_estatisticas_soma, 120))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_estatisticas_pares_impares, 7, 8))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_estatisticas_moldura_miolo, 16, 9))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun exibeAsFixasComOpcaoDeLimpar() {
        mostrarTelaDetalhe()

        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_fixas_rotulo, "01 · 02"))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_fixas_limpar))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun tocarLimparFixasDisparaOCallback() {
        var limpou = false

        mostrarTelaDetalhe(onLimparFixasClick = { limpou = true })

        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_fixas_limpar))
            .performScrollTo()
            .performClick()

        assertTrue(limpou)
    }

    @Test
    fun tocarEditarDisparaOCallback() {
        var editou = false

        mostrarTelaDetalhe(onEditarClick = { editou = true })

        composeTestRule.onNodeWithText(context.getString(R.string.detalhe_editar_cta)).performClick()

        assertTrue(editou)
    }

    @Test
    fun tocarRefazerDisparaOCallback() {
        var refez = false

        mostrarTelaDetalhe(onRefazerClick = { refez = true })

        composeTestRule.onNodeWithTag(tagBotaoRefazer()).performClick()

        assertTrue(refez)
    }

    @Test
    fun tocarExcluirDisparaOCallback() {
        var excluiu = false

        mostrarTelaDetalhe(onExcluirClick = { excluiu = true })

        composeTestRule.onNodeWithTag(tagBotaoExcluirDetalhe()).performClick()

        assertTrue(excluiu)
    }

    @Test
    fun comPedidoDeExclusaoPendenteExibeODialogo() {
        mostrarTelaDetalhe(uiState = estadoDeExemplo.copy(palpiteParaConfirmarExclusao = true))

        composeTestRule
            .onNodeWithText(context.getString(R.string.home_excluir_confirmar_titulo))
            .assertIsDisplayed()
    }

    @Test
    fun modoEdicaoExibeAContagemEBloqueiaSalvarComContagemErrada() {
        val estadoDeEdicao =
            estadoDeExemplo.copy(modoEdicao = true, dezenasEmEdicao = (1..14).toSet())

        mostrarTelaDetalhe(uiState = estadoDeEdicao)

        composeTestRule.onNodeWithText(context.getString(R.string.detalhe_editar_contagem, 14, 15)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(tagBotaoSalvarEdicao()).assertIsNotEnabled()
    }

    @Test
    fun modoEdicaoComContagemCertaHabilitaSalvarEDisparaOCallback() {
        var salvou = false
        val estadoDeEdicao = estadoDeExemplo.copy(modoEdicao = true, dezenasEmEdicao = (1..15).toSet())

        mostrarTelaDetalhe(uiState = estadoDeEdicao, onSalvarEdicaoClick = { salvou = true })

        composeTestRule.onNodeWithTag(tagBotaoSalvarEdicao()).performClick()

        assertTrue(salvou)
    }

    @Test
    fun tocarUmaDezenaNaoFixaEmModoEdicaoDisparaOCallbackComADezenaCorreta() {
        var dezenaTocada: Int? = null
        val estadoDeEdicao = estadoDeExemplo.copy(modoEdicao = true, dezenasEmEdicao = (1..15).toSet())

        mostrarTelaDetalhe(uiState = estadoDeEdicao, onDezenaClick = { dezenaTocada = it })

        composeTestRule.onNodeWithTag(tagDezenaNaGrade(10)).performClick()

        assertEquals(10, dezenaTocada)
    }

    @Test
    fun cancelarNoModoEdicaoDisparaOCallback() {
        var cancelou = false
        val estadoDeEdicao = estadoDeExemplo.copy(modoEdicao = true, dezenasEmEdicao = (1..15).toSet())

        mostrarTelaDetalhe(uiState = estadoDeEdicao, onCancelarEdicaoClick = { cancelou = true })

        composeTestRule.onNodeWithText(context.getString(R.string.detalhe_editar_cancelar)).performClick()

        assertTrue(cancelou)
    }

    @Test
    fun palpiteNaoEncontradoExibeMensagemEmVezDaGrade() {
        mostrarTelaDetalhe(uiState = DetalheUiState(carregando = false, palpiteExiste = false))

        composeTestRule.onNodeWithText(context.getString(R.string.detalhe_nao_encontrado)).assertIsDisplayed()
    }

    @Test
    fun comFechamentoAcimaDe15ExibeOLinkDeDesdobramentosEDisparaOCallback() {
        var abriu = false
        val estadoComFechamentoMaior = estadoDeExemplo.copy(quantidadeDeDezenas = 18, podeVerDesdobramentos = true)

        mostrarTelaDetalhe(uiState = estadoComFechamentoMaior, onVerDesdobramentosClick = { abriu = true })

        composeTestRule.onNodeWithText(context.getString(R.string.detalhe_ver_desdobramentos_cta)).performClick()

        assertTrue(abriu)
    }
}
