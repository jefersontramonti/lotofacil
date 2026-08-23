package com.trevo.app.detalhe

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        onCompartilharClick: () -> Unit = {},
        onFecharCompartilharClick: () -> Unit = {},
        onEnviarWhatsAppClick: (String) -> Unit = {},
        onCopiarTextoClick: (String) -> Unit = {},
        onExportarClick: () -> Unit = {},
        onExportarBloqueadoClick: () -> Unit = {},
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
                    onCompartilharClick = onCompartilharClick,
                    onFecharCompartilharClick = onFecharCompartilharClick,
                    onEnviarWhatsAppClick = onEnviarWhatsAppClick,
                    onCopiarTextoClick = onCopiarTextoClick,
                    onExportarClick = onExportarClick,
                    onExportarBloqueadoClick = onExportarBloqueadoClick,
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
    fun tocarCompartilharDisparaOCallback() {
        var abriu = false

        mostrarTelaDetalhe(onCompartilharClick = { abriu = true })

        composeTestRule.onNodeWithTag(tagBotaoCompartilhar()).performClick()

        assertTrue(abriu)
    }

    @Test
    fun semIsProTocarExportarDisparaOCallbackDeBloqueado() {
        var bloqueado = false
        var exportou = false

        mostrarTelaDetalhe(
            uiState = estadoDeExemplo.copy(isPro = false),
            onExportarClick = { exportou = true },
            onExportarBloqueadoClick = { bloqueado = true },
        )

        composeTestRule.onNodeWithTag(tagBotaoExportar()).performClick()

        assertTrue(bloqueado)
        assertFalse(exportou)
    }

    @Test
    fun comIsProTocarExportarDisparaOCallbackDeExportar() {
        var bloqueado = false
        var exportou = false

        mostrarTelaDetalhe(
            uiState = estadoDeExemplo.copy(isPro = true),
            onExportarClick = { exportou = true },
            onExportarBloqueadoClick = { bloqueado = true },
        )

        composeTestRule.onNodeWithTag(tagBotaoExportar()).performClick()

        assertTrue(exportou)
        assertFalse(bloqueado)
    }

    private val crencasTextoDeExemplo: String
        get() = context.resources.getQuantityString(R.plurals.detalhe_compartilhar_crencas, 1, 1)

    @Test
    fun folhaDeCompartilhamentoExibeAMensagemSemNumeroDeConcursoQuandoAindaDesconhecido() {
        mostrarTelaDetalhe(uiState = estadoDeExemplo.copy(compartilhando = true))

        val dezenas = (1..15).joinToString(" · ") { "%02d".format(it) }
        composeTestRule
            .onNodeWithText(
                context.getString(R.string.detalhe_compartilhar_mensagem_sem_concurso, dezenas, crencasTextoDeExemplo),
            ).assertIsDisplayed()
    }

    @Test
    fun folhaDeCompartilhamentoComNumeroDeConcursoIncluiOConcursoNaMensagem() {
        mostrarTelaDetalhe(uiState = estadoDeExemplo.copy(compartilhando = true, numeroDoConcurso = 3457))

        val dezenas = (1..15).joinToString(" · ") { "%02d".format(it) }
        composeTestRule
            .onNodeWithText(
                context.getString(
                    R.string.detalhe_compartilhar_mensagem_com_concurso,
                    3457,
                    dezenas,
                    crencasTextoDeExemplo,
                ),
            ).assertIsDisplayed()
    }

    @Test
    fun tocarEnviarNoWhatsAppDisparaOCallbackComAMensagemMontada() {
        var mensagemRecebida: String? = null

        mostrarTelaDetalhe(
            uiState = estadoDeExemplo.copy(compartilhando = true),
            onEnviarWhatsAppClick = { mensagemRecebida = it },
        )

        composeTestRule.onNodeWithTag(tagBotaoEnviarWhatsApp()).performClick()

        val dezenas = (1..15).joinToString(" · ") { "%02d".format(it) }
        assertEquals(
            context.getString(R.string.detalhe_compartilhar_mensagem_sem_concurso, dezenas, crencasTextoDeExemplo),
            mensagemRecebida,
        )
    }

    @Test
    fun tocarCopiarOTextoDisparaOCallbackEExibeAConfirmacao() {
        var copiou = false

        mostrarTelaDetalhe(
            uiState = estadoDeExemplo.copy(compartilhando = true),
            onCopiarTextoClick = { copiou = true },
        )

        composeTestRule.onNodeWithTag(tagBotaoCopiarTexto()).performClick()

        assertTrue(copiou)
    }

    @Test
    fun comCopiadoVerdadeiroExibeAConfirmacaoDeProntoParaEnviar() {
        mostrarTelaDetalhe(uiState = estadoDeExemplo.copy(compartilhando = true, copiado = true))

        composeTestRule
            .onNodeWithText(context.getString(R.string.detalhe_compartilhar_copiado_confirmacao))
            .assertIsDisplayed()
    }

    @Test
    fun semIsProOSeletorDeFechamentoMostraOsTamanhosMaioresBloqueados() {
        mostrarTelaDetalhe(uiState = estadoDeExemplo.copy(isPro = false))

        composeTestRule.onNodeWithText("16 🔒").assertIsDisplayed()
    }

    @Test
    fun comIsProOSeletorDeFechamentoDestravaOsTamanhosMaiores() {
        // "16" sozinho é ambíguo (a grade também tem a dezena 16 como
        // célula, RF-04.1) — a ausência do cadeado é a prova inequívoca do
        // destravamento.
        mostrarTelaDetalhe(uiState = estadoDeExemplo.copy(isPro = true))

        composeTestRule.onAllNodesWithText("16 🔒").assertCountEquals(0)
    }

    @Test
    fun comFechamentoAcimaDe15ExibeOLinkDeDesdobramentosEDisparaOCallback() {
        var abriu = false
        val estadoComFechamentoMaior = estadoDeExemplo.copy(quantidadeDeDezenas = 18, podeVerDesdobramentos = true)

        mostrarTelaDetalhe(uiState = estadoComFechamentoMaior, onVerDesdobramentosClick = { abriu = true })

        composeTestRule.onNodeWithText(context.getString(R.string.detalhe_ver_desdobramentos_cta)).performClick()

        assertTrue(abriu)
    }

    // RNF-03.1 — os ícones do cabeçalho (📤/⤓/↻/🗑) eram alvos de toque sem
    // tamanho garantido (achado de auditoria de acessibilidade, 2026-08-23),
    // corrigidos envolvendo cada glifo num Box(size = 48.dp), mesmo padrão
    // de BotaoVoltar — Text isolado ignora constraints de tamanho mínimo.
    @Test
    fun botaoExcluirTemAlvoDeToqueDeAoMenos48dp() {
        mostrarTelaDetalhe()

        composeTestRule
            .onNodeWithTag(tagBotaoExcluirDetalhe())
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    // RNF-03.4 — "guardar como fixas" tinha um Checkbox decorativo ao lado
    // (onCheckedChange = null) e o clique não expunha o estado marcado/
    // desmarcado ao TalkBack; trocado por Modifier.toggleable (mesmo achado).
    @Test
    fun guardarComoFixasExpoeOEstadoMarcadoAoTalkBack() {
        val estadoDeEdicao =
            estadoDeExemplo.copy(
                modoEdicao = true,
                dezenasEmEdicao = (1..15).toSet(),
                guardarComoFixasAoSalvar = true,
            )

        mostrarTelaDetalhe(uiState = estadoDeEdicao)

        composeTestRule.onNodeWithTag(tagGuardarComoFixas()).performScrollTo().assertIsOn()
    }

    @Test
    fun tocarGuardarComoFixasDisparaOCallback() {
        var alternou = false
        val estadoDeEdicao =
            estadoDeExemplo.copy(
                modoEdicao = true,
                dezenasEmEdicao = (1..15).toSet(),
                guardarComoFixasAoSalvar = false,
            )

        mostrarTelaDetalhe(uiState = estadoDeEdicao, onAlternarGuardarFixasClick = { alternou = true })

        composeTestRule
            .onNodeWithTag(tagGuardarComoFixas())
            .performScrollTo()
            .assertIsOff()
            .performClick()

        assertTrue(alternou)
    }
}
