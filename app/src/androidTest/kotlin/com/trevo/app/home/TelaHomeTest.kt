package com.trevo.app.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.engine.crenca.FaseDaLua
import com.trevo.core.engine.crenca.GRUPOS_DO_BICHO
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * RF-03 — Home. Wireframes 1d ("Home · lista de palpites"), 1e ("Home ·
 * limite atingido" — RF-09.1/09.2, anúncio recompensado e CTA de assinar)
 * e 1t ("Card do sonho").
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

    private val grupoCobra = GRUPOS_DO_BICHO.first { it.numero == 9 }

    private fun mostrarTelaHome(
        uiState: HomeUiState = HomeUiState(carregando = false),
        onExcluirClick: (Long) -> Unit = {},
        onConfirmarExclusaoClick: () -> Unit = {},
        onCancelarExclusaoClick: () -> Unit = {},
        onAlternarListaDeGruposClick: () -> Unit = {},
        onGrupoClick: (Int) -> Unit = {},
        onFecharDialogoSonhoClick: () -> Unit = {},
        onConfirmarSonhoClick: (Int) -> Unit = {},
        onAssistirAnuncioClick: () -> Unit = {},
        onAssinarClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaHome(
                    uiState = uiState,
                    onExcluirClick = onExcluirClick,
                    onConfirmarExclusaoClick = onConfirmarExclusaoClick,
                    onCancelarExclusaoClick = onCancelarExclusaoClick,
                    onAlternarListaDeGruposClick = onAlternarListaDeGruposClick,
                    onGrupoClick = onGrupoClick,
                    onFecharDialogoSonhoClick = onFecharDialogoSonhoClick,
                    onConfirmarSonhoClick = onConfirmarSonhoClick,
                    onAssistirAnuncioClick = onAssistirAnuncioClick,
                    onAssinarClick = onAssinarClick,
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
    fun comDezenasNovasExibeALinhaDeDiff() {
        val palpiteComDiff = palpiteDeExemplo.copy(dezenasNovas = listOf(3, 9))

        mostrarTelaHome(uiState = HomeUiState(carregando = false, palpitesHoje = listOf(palpiteComDiff)))

        composeTestRule
            .onNodeWithText(
                context.resources.getQuantityString(R.plurals.home_diff_dezenas_novas, 2, 2, "03 · 09"),
            ).assertIsDisplayed()
    }

    @Test
    fun semPerfilNaoExibeSaudacaoNemIndiceDeSorte() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, nome = null))

        composeTestRule.onNodeWithText(context.getString(R.string.home_sorte_titulo)).assertDoesNotExist()
    }

    @Test
    fun comPerfilExibeSaudacaoComOPrimeiroNomeEOIndiceDeSorte() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    nome = "Marlene Silva",
                    indiceDeSorte = 74,
                    faseDaLua = FaseDaLua.CRESCENTE_INICIAL,
                    signo = Signo.CANCER,
                    diaDaSemanaAbreviado = "seg",
                ),
        )

        composeTestRule.onNodeWithText(context.getString(R.string.home_saudacao, "Marlene")).assertIsDisplayed()
        composeTestRule.onNodeWithText("74").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.fase_lua_crescente_inicial)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.signo_cancer)).assertIsDisplayed()
    }

    @Test
    fun semCrencaSonhoAtivaNaoExibeOSeletorDeGrupos() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, crencaSonhoAtiva = false))

        composeTestRule.onNodeWithText(context.getString(R.string.home_sonho_titulo)).assertDoesNotExist()
    }

    @Test
    fun comCrencaSonhoAtivaExibeAPreviaDosGrupos() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    crencaSonhoAtiva = true,
                    gruposDoSonhoPreview = GRUPOS_DO_BICHO.take(4),
                ),
        )

        composeTestRule.onNodeWithText(context.getString(R.string.home_sonho_titulo)).assertIsDisplayed()
        GRUPOS_DO_BICHO.take(4).forEach { grupo ->
            composeTestRule.onNodeWithTag(tagGrupoDoBicho(grupo.numero)).assertIsDisplayed()
        }
    }

    @Test
    fun semSonhoConfirmadoHojeNaoExibeALinhaDeSonhoAtual() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    crencaSonhoAtiva = true,
                    gruposDoSonhoPreview = GRUPOS_DO_BICHO.take(10),
                    grupoDoSonhoConfirmadoHoje = null,
                ),
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.home_sonho_atual, grupoCobra.nome))
            .assertDoesNotExist()
    }

    @Test
    fun comSonhoConfirmadoHojeExibeONomeDoGrupoNaLinhaDeSonhoAtual() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    crencaSonhoAtiva = true,
                    gruposDoSonhoPreview = GRUPOS_DO_BICHO.take(10),
                    grupoDoSonhoConfirmadoHoje = grupoCobra.numero,
                ),
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.home_sonho_atual, grupoCobra.nome))
            .assertIsDisplayed()
    }

    @Test
    fun tocarVerOs25GruposDisparaOnAlternarListaDeGruposClick() {
        var alternado = false

        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    crencaSonhoAtiva = true,
                    gruposDoSonhoPreview = GRUPOS_DO_BICHO.take(4),
                ),
            onAlternarListaDeGruposClick = { alternado = true },
        )

        composeTestRule.onNodeWithTag(TAG_BOTAO_VER_GRUPOS).performClick()

        assertTrue(alternado)
    }

    @Test
    fun comListaExpandidaExibeOsGruposCompletos() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    crencaSonhoAtiva = true,
                    gruposDoSonhoPreview = GRUPOS_DO_BICHO.take(4),
                    listaDeGruposExpandida = true,
                ),
        )

        composeTestRule.onNodeWithTag(tagGrupoDoBicho(25)).assertIsDisplayed()
    }

    @Test
    fun tocarUmGrupoDisparaOnGrupoClickComONumeroCorreto() {
        var numeroTocado: Int? = null

        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    crencaSonhoAtiva = true,
                    gruposDoSonhoPreview = listOf(grupoCobra),
                ),
            onGrupoClick = { numeroTocado = it },
        )

        composeTestRule.onNodeWithTag(tagGrupoDoBicho(grupoCobra.numero)).performClick()

        assertEquals(grupoCobra.numero, numeroTocado)
    }

    @Test
    fun comGrupoAbertoExibeNomeLeituraDezenasEODisclaimerObrigatorio() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, grupoAbertoNoDialog = grupoCobra))

        composeTestRule.onNodeWithText(grupoCobra.nome).assertIsDisplayed()
        composeTestRule.onNodeWithText(grupoCobra.leituraPopular).assertIsDisplayed()
        composeTestRule.onNodeWithText("09 · 17").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.home_sonho_card_disclaimer)).assertIsDisplayed()
    }

    @Test
    fun confirmarNoCartaoDoSonhoDisparaOnConfirmarSonhoClickComONumeroDoGrupo() {
        var numeroConfirmado: Int? = null

        mostrarTelaHome(
            uiState = HomeUiState(carregando = false, grupoAbertoNoDialog = grupoCobra),
            onConfirmarSonhoClick = { numeroConfirmado = it },
        )

        composeTestRule.onNodeWithText(context.getString(R.string.home_sonho_card_confirmar_cta)).performClick()

        assertEquals(grupoCobra.numero, numeroConfirmado)
    }

    @Test
    fun grupoJaConfirmadoHojeExibeOEstadoConfirmadoEmVezDoCta() {
        mostrarTelaHome(
            uiState =
                HomeUiState(
                    carregando = false,
                    grupoAbertoNoDialog = grupoCobra,
                    grupoDoSonhoConfirmadoHoje = grupoCobra.numero,
                ),
        )

        composeTestRule.onNodeWithText(context.getString(R.string.home_sonho_card_confirmado)).assertIsDisplayed()
    }

    @Test
    fun fecharODialogoDoSonhoDisparaOnFecharDialogoSonhoClick() {
        var fechado = false

        mostrarTelaHome(
            uiState = HomeUiState(carregando = false, grupoAbertoNoDialog = grupoCobra),
            onFecharDialogoSonhoClick = { fechado = true },
        )

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.home_sonho_card_fechar_descricao))
            .performClick()

        assertTrue(fechado)
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
                put("home_sonho_card_disclaimer", context.getString(R.string.home_sonho_card_disclaimer))
                put("home_assistir_anuncio_cta", context.getString(R.string.home_assistir_anuncio_cta))
                put("home_assinar_cta", context.getString(R.string.home_assinar_cta))
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

    @Test
    fun comPalpiteGratisRestanteExibeATextoDeRestantesEOCtaNormal() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, isPro = false, palpitesGratisRestantesHoje = 1))

        composeTestRule
            .onNodeWithText(context.resources.getQuantityString(R.plurals.home_restantes_gratis, 1, 1))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTAO_CTA_PRINCIPAL).assertIsDisplayed()
    }

    @Test
    fun semGratisRestanteExibeOCtaDeAnuncioEDeAssinarEmVezDoGerar() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, isPro = false, palpitesGratisRestantesHoje = 0))

        composeTestRule.onNodeWithTag(TAG_BOTAO_ASSISTIR_ANUNCIO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTAO_ASSINAR).assertIsDisplayed()
    }

    @Test
    fun tocarAssistirAnuncioDisparaOCallback() {
        var assistiu = false
        mostrarTelaHome(
            uiState = HomeUiState(carregando = false, isPro = false, palpitesGratisRestantesHoje = 0),
            onAssistirAnuncioClick = { assistiu = true },
        )

        composeTestRule.onNodeWithTag(TAG_BOTAO_ASSISTIR_ANUNCIO).performClick()

        assertTrue(assistiu)
    }

    @Test
    fun tocarAssinarNoLimiteDisparaOCallback() {
        var assinou = false
        mostrarTelaHome(
            uiState = HomeUiState(carregando = false, isPro = false, palpitesGratisRestantesHoje = 0),
            onAssinarClick = { assinou = true },
        )

        composeTestRule.onNodeWithTag(TAG_BOTAO_ASSINAR).performClick()

        assertTrue(assinou)
    }

    @Test
    fun proExibeOTextoDePalpitesIlimitadosMesmoComRestantesZerado() {
        mostrarTelaHome(uiState = HomeUiState(carregando = false, isPro = true, palpitesGratisRestantesHoje = 0))

        composeTestRule.onNodeWithText(context.getString(R.string.home_restantes_pro)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_BOTAO_CTA_PRINCIPAL).assertIsDisplayed()
    }
}
