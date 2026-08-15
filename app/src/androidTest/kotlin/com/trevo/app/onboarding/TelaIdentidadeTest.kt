package com.trevo.app.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.app.R
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.ui.TrevoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * RF-01.2 — "Coletar nome e data de nascimento em campos separados."
 * RF-01.3 — "Validar a data no formato dd/mm/aaaa: mês entre 1 e 12, dia
 * existente no mês (considerando ano bissexto), ano entre 1900 e a data
 * atual."
 * RF-01.9 — "Formatar automaticamente a data de nascimento enquanto o
 * usuário digita apenas números, inserindo as barras dd/mm/aaaa (ex.:
 * "12081986" exibido como "12/08/1986"), sem alterar as regras de
 * validação de RF-01.3."
 * RF-01.4 — "Bloquear o cadastro de menores de 18 anos, com mensagem
 * explícita." Casos T1–T3 (ver `.claude/handoff.md`): a mensagem de
 * `ErroDataNascimento.MENOR_DE_IDADE` aparece sob o mesmo
 * `TAG_ERRO_NASCIMENTO` dos erros de RF-01.3, o CTA "Continuar" continua
 * habilitado (RF-01.6 fora de escopo) e a string nova entra na checagem de
 * conformidade da regra inviolável 2.
 *
 * Dirige o [TelaIdentidade] stateless (sem Hilt), içando o estado
 * localmente no teste — a fonte da verdade do estado real é
 * [IdentidadeViewModel], coberto por `IdentidadeViewModelTest` (JVM); o
 * cálculo do erro em si é coberto por `ValidadorDataNascimentoTest`
 * (`:core:engine`), e a regra da máscara em si (dígito a dígito, cursor,
 * colagem) é coberta por `MascaraDataNascimentoTest` (`:core:engine`).
 * Este arquivo cobre a renderização — a mensagem certa aparece sob o
 * `TAG_ERRO_NASCIMENTO` quando `uiState.erroNascimento` não é nulo — e,
 * desde RF-01.9, também a ponta a ponta pela UI real: digitar dígitos
 * crus no campo de nascimento precisa exibir o texto já com as barras e
 * entregar esse mesmo texto formatado ao callback. Nada mais muda de
 * comportamento (RF-01.6/RF-01.5 seguem fora de escopo).
 *
 * Cobre também que o escopo não vazou de requisitos vizinhos: sem cartão
 * de signo (RF-01.5) e sem bloqueio do CTA "Continuar" mesmo com erro
 * exibido (RF-01.6), que compartilham a mesma tela no wireframe 1b mas
 * não são parte desta entrega.
 *
 * [TelaIdentidade], `TAG_CAMPO_NOME`, `TAG_CAMPO_NASCIMENTO`,
 * `TAG_ERRO_NASCIMENTO` e as strings `identidade_*` (incluindo as quatro
 * novas `identidade_erro_*` de RF-01.3) ainda não existem em produção:
 * este arquivo deve falhar a compilação até que o `trevo-developer` os
 * implemente em `app/src/main/kotlin/com/trevo/app/onboarding/TelaIdentidade.kt`
 * e `app/src/main/res/values/strings.xml`.
 */
@RunWith(AndroidJUnit4::class)
class TelaIdentidadeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val titulo get() = context.getString(R.string.identidade_titulo)
    private val subtitulo get() = context.getString(R.string.identidade_subtitulo)
    private val labelNome get() = context.getString(R.string.identidade_label_nome)
    private val placeholderNome get() = context.getString(R.string.identidade_placeholder_nome)
    private val labelNascimento get() = context.getString(R.string.identidade_label_nascimento)
    private val placeholderNascimento get() = context.getString(R.string.identidade_placeholder_nascimento)
    private val ctaVoltar get() = context.getString(R.string.identidade_cta_voltar)
    private val ctaContinuar get() = context.getString(R.string.identidade_cta_continuar)
    private val progressoDescricao get() = context.getString(R.string.identidade_progresso_descricao)
    private val erroFormato get() = context.getString(R.string.identidade_erro_formato)
    private val erroMes get() = context.getString(R.string.identidade_erro_mes)
    private val erroDia get() = context.getString(R.string.identidade_erro_dia)
    private val erroIntervalo get() = context.getString(R.string.identidade_erro_intervalo)
    private val erroMenorDeIdade get() = context.getString(R.string.identidade_erro_menor_idade)

    // RF-01.5 — cartão "SEU SIGNO" (plano `.claude/handoff.md`, decisão D6).
    private val signoRotulo get() = context.getString(R.string.identidade_signo_rotulo)
    private val signoMarcadorNeutro get() = context.getString(R.string.identidade_signo_marcador_neutro)
    private val signoSeparadorDezenas get() = context.getString(R.string.identidade_signo_separador_dezenas)
    private val signoDescricaoFormato get() = context.getString(R.string.identidade_signo_descricao)
    private val signoDescricaoNeutro get() = context.getString(R.string.identidade_signo_descricao_neutro)
    private val nomeSignoCapricornio get() = context.getString(R.string.signo_capricornio)
    private val nomeSignoAquario get() = context.getString(R.string.signo_aquario)
    private val nomeSignoPeixes get() = context.getString(R.string.signo_peixes)
    private val nomeSignoAries get() = context.getString(R.string.signo_aries)
    private val nomeSignoTouro get() = context.getString(R.string.signo_touro)
    private val nomeSignoGemeos get() = context.getString(R.string.signo_gemeos)
    private val nomeSignoCancer get() = context.getString(R.string.signo_cancer)
    private val nomeSignoLeao get() = context.getString(R.string.signo_leao)
    private val nomeSignoVirgem get() = context.getString(R.string.signo_virgem)
    private val nomeSignoLibra get() = context.getString(R.string.signo_libra)
    private val nomeSignoEscorpiao get() = context.getString(R.string.signo_escorpiao)
    private val nomeSignoSagitario get() = context.getString(R.string.signo_sagitario)

    // Mesma lista negativa de TelaAberturaTest — CLAUDE.md, regra
    // inviolável 2: nenhum texto pode insinuar aumento de chance.
    private val expressoesProibidas =
        listOf(
            "aumenta",
            "garante",
            "mais chance",
            "melhora sua chance",
        )

    // Os 12 signos do zodíaco ocidental, fonte: `Docs/Trevo -
    // Lotofácil.dc.html`, array `SIGNOS` (protótipo de referência do
    // motor de crenças). Nenhum desses nomes aparece no subtítulo
    // aprovado desta entrega ("Usado pela numerologia do nome e pelo
    // cálculo do signo.") — só a palavra "signo" em si aparece, por
    // isso a checagem de ausência do cartão de RF-01.5 usa os nomes dos
    // signos (correspondência exata) em vez da palavra "signo"
    // (substring), que colidiria com esse subtítulo legítimo.
    private val nomesDosSignos =
        listOf(
            "Áries",
            "Touro",
            "Gêmeos",
            "Câncer",
            "Leão",
            "Virgem",
            "Libra",
            "Escorpião",
            "Sagitário",
            "Capricórnio",
            "Aquário",
            "Peixes",
        )

    // Reservado (e agora usado) para o cartão "SEU SIGNO" de RF-01.5
    // (wireframe 1b). O valor precisa ser exatamente "cartao_signo" —
    // plano `.claude/handoff.md`, decisão D5: `TAG_CARTAO_SIGNO` ainda
    // não existe em produção (`TelaIdentidade.kt`), então este teste
    // compara contra o literal, não contra a constante.
    private val tagCartaoSigno = "cartao_signo"

    // Formato do protótipo (`Docs/Trevo - Lotofácil.dc.html`, `pad`/`lista`,
    // plano `.claude/handoff.md`, decisão D5): dois dígitos com zero à
    // esquerda, separados por " · ".
    private fun dezenasFormatadas(vararg dezenas: Int): String =
        dezenas.joinToString(separator = signoSeparadorDezenas) { it.toString().padStart(2, '0') }

    private fun mostrarTelaIdentidade(
        uiState: IdentidadeUiState = IdentidadeUiState(),
        onNomeChange: (String) -> Unit = {},
        onNascimentoChange: (String) -> Unit = {},
        onVoltarClick: () -> Unit = {},
        onContinuarClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TrevoTheme {
                TelaIdentidade(
                    uiState = uiState,
                    onNomeChange = onNomeChange,
                    onNascimentoChange = onNascimentoChange,
                    onVoltarClick = onVoltarClick,
                    onContinuarClick = onContinuarClick,
                )
            }
        }
    }

    @Test
    fun camposDeNomeENascimentoExistemSimultaneamenteComoNosDeTextoDistintos() {
        mostrarTelaIdentidade()

        composeTestRule.onNodeWithTag(TAG_CAMPO_NOME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_CAMPO_NASCIMENTO).assertIsDisplayed()

        // Rótulos de cada campo aparecem separadamente na árvore — prova de
        // que são dois campos distintos, não um único campo combinado.
        composeTestRule.onNodeWithText(labelNome).assertIsDisplayed()
        composeTestRule.onNodeWithText(labelNascimento).assertIsDisplayed()
    }

    @Test
    fun digitarNoCampoNomeDisparaOnNomeChangeSemAlterarNascimento() {
        var uiState by mutableStateOf(IdentidadeUiState())

        mostrarTelaIdentidade(
            uiState = uiState,
            onNomeChange = { uiState = uiState.copy(nome = it) },
            onNascimentoChange = { uiState = uiState.copy(nascimento = it) },
        )

        composeTestRule.onNodeWithTag(TAG_CAMPO_NOME).performTextInput("Marlene")
        composeTestRule.waitForIdle()

        assertEquals("Marlene", uiState.nome)
        assertEquals("", uiState.nascimento)
    }

    @Test
    fun digitarNoCampoNascimentoDisparaOnNascimentoChangeSemAlterarNome() {
        var uiState by mutableStateOf(IdentidadeUiState())

        mostrarTelaIdentidade(
            uiState = uiState,
            onNomeChange = { uiState = uiState.copy(nome = it) },
            onNascimentoChange = { uiState = uiState.copy(nascimento = it) },
        )

        composeTestRule.onNodeWithTag(TAG_CAMPO_NASCIMENTO).performTextInput("14/07/1978")
        composeTestRule.waitForIdle()

        // "14/07/1978" já está no formato dd/mm/aaaa: a máscara de RF-01.9 é
        // idempotente sobre ele, então este teste também prova idempotência
        // pela UI, além de continuar provando o disparo isolado do callback.
        assertEquals("14/07/1978", uiState.nascimento)
        assertEquals("", uiState.nome)
    }

    @Test
    fun digitarSoDigitosNoCampoNascimentoExibeAsBarrasInseridasPelaMascara() {
        // RF-01.9: digitar "12081986" (exemplo literal do requisito) precisa
        // exibir "12/08/1986" no campo e entregar o mesmo valor já formatado
        // ao callback — não os dígitos crus.
        var uiState by mutableStateOf(IdentidadeUiState())

        mostrarTelaIdentidade(
            uiState = uiState,
            onNomeChange = { uiState = uiState.copy(nome = it) },
            onNascimentoChange = { uiState = uiState.copy(nascimento = it) },
        )

        composeTestRule.onNodeWithTag(TAG_CAMPO_NASCIMENTO).performTextInput("12081986")
        composeTestRule.waitForIdle()

        assertEquals("12/08/1986", uiState.nascimento)
        composeTestRule.onNodeWithText("12/08/1986").assertIsDisplayed()
    }

    @Test
    fun digitarNascimentoJaFormatadoNoCampoNaoDuplicaBarrasIdempotente() {
        // Colar/digitar um valor já com as barras ("12/08/1986") não pode
        // produzir barra duplicada nem qualquer outra distorção — a máscara
        // é idempotente (mesma garantia coberta em MascaraDataNascimentoTest,
        // aqui verificada de ponta a ponta pela UI).
        var uiState by mutableStateOf(IdentidadeUiState())

        mostrarTelaIdentidade(
            uiState = uiState,
            onNomeChange = { uiState = uiState.copy(nome = it) },
            onNascimentoChange = { uiState = uiState.copy(nascimento = it) },
        )

        composeTestRule.onNodeWithTag(TAG_CAMPO_NASCIMENTO).performTextInput("12/08/1986")
        composeTestRule.waitForIdle()

        assertEquals("12/08/1986", uiState.nascimento)
        composeTestRule.onNodeWithText("12/08/1986").assertIsDisplayed()
    }

    /**
     * Achado A3 do `trevo-reviewer` (`.claude/handoff.md`, seção "Achados"):
     * o bloco de reconciliação do `TextFieldValue` local com
     * `uiState.nascimento` (`TelaIdentidade.kt`, hoje dois
     * `rememberSaveable` comparados a cada recomposição — o `developer`
     * vai trocar por `LaunchedEffect(uiState.nascimento) { ... }`) nunca
     * é exercitado por nenhum teste existente, porque [mostrarTelaIdentidade]
     * recebe `uiState` como **parâmetro comum** (um valor congelado no
     * momento da chamada de `setContent`, não um `State` observável) —
     * reatribuir a variável do corpo do teste não recompõe [TelaIdentidade],
     * que nunca lê essa variável de novo. Os dois testes abaixo içam o
     * estado de verdade **dentro** de `setContent`, lendo `estado.value`
     * diretamente no corpo do Composable (não através de um parâmetro
     * comum), para que o sistema de snapshot do Compose rastreie a leitura
     * e recomponha quando o teste alterar `estado.value` de fora.
     */
    @Test
    fun mudancaExternaDeUiStateNascimentoReconciliaOCampoLocalComONovoValor() {
        // Simula RF-07.1 (ou restauração de processo): algo fora da
        // digitação normal muda `uiState.nascimento` depois da composição
        // inicial. Diferente de `mostrarTelaIdentidade`, `estado.value` é
        // lido diretamente dentro do `setContent` — uma mudança externa de
        // verdade, não um parâmetro congelado.
        val estado = mutableStateOf(IdentidadeUiState())

        composeTestRule.setContent {
            TrevoTheme {
                TelaIdentidade(
                    uiState = estado.value,
                    onNomeChange = { estado.value = estado.value.copy(nome = it) },
                    onNascimentoChange = { estado.value = estado.value.copy(nascimento = it) },
                    onVoltarClick = {},
                    onContinuarClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TAG_CAMPO_NASCIMENTO).performTextInput("12")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()

        // Mudança externa genuína: nada que o campo digitou produziria
        // "14/07/1978" a partir de "12" — só uma reconciliação de verdade
        // (não o eco do próprio `onNascimentoChange`) explicaria o campo
        // passar a exibir esse valor.
        composeTestRule.runOnUiThread {
            estado.value = estado.value.copy(nascimento = "14/07/1978")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("14/07/1978").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("12", substring = false).assertCountEquals(0)
    }

    @Test
    fun digitacaoSequencialSemMudancaExternaDeUiStateNaoPerdeTextoDigitado() {
        // Regressão a não repetir (já corrigida uma vez pelo developer,
        // ver ".claude/handoff.md", seção "Divergência em relação ao
        // plano"): no fluxo normal, `onNascimentoChange` só ecoa de volta
        // exatamente o que a própria tela já formatou — não há mudança
        // "externa" nenhuma. Digitar sequencialmente, dígito a dígito,
        // precisa produzir "12/08/1986" (exemplo do requisito RF-01.9)
        // sem nenhum caractere perdido no caminho.
        val estado = mutableStateOf(IdentidadeUiState())

        composeTestRule.setContent {
            TrevoTheme {
                TelaIdentidade(
                    uiState = estado.value,
                    onNomeChange = { estado.value = estado.value.copy(nome = it) },
                    onNascimentoChange = { estado.value = estado.value.copy(nascimento = it) },
                    onVoltarClick = {},
                    onContinuarClick = {},
                )
            }
        }

        "12081986".forEach { digito ->
            composeTestRule.onNodeWithTag(TAG_CAMPO_NASCIMENTO).performTextInput(digito.toString())
            composeTestRule.waitForIdle()
        }

        assertEquals("12/08/1986", estado.value.nascimento)
        composeTestRule.onNodeWithText("12/08/1986").assertIsDisplayed()
    }

    @Test
    fun naoExibeErroDeValidacaoNemNomeDeSignoNemDesabilitaContinuarECartaoDeSignoApareceComMarcadorNeutro() {
        // `IdentidadeUiState()` padrão tem `erroNascimento == null` (campo
        // vazio, ErroDataNascimento.VAZIO não vira mensagem — ver RF-01.3) e
        // `signo == null`. RF-01.6 (travar avanço com erro) segue fora de
        // escopo. RF-01.5 (cartão de signo) passou a estar DENTRO do escopo
        // desta tela: o cartão agora é sempre renderizado (plano
        // `.claude/handoff.md`, decisão D5), só que com o marcador neutro —
        // nenhum nome de signo pode aparecer.
        mostrarTelaIdentidade()

        composeTestRule
            .onAllNodesWithText("erro", substring = true, ignoreCase = true)
            .assertCountEquals(0)

        composeTestRule
            .onAllNodes(hasTestTag(TAG_ERRO_NASCIMENTO))
            .assertCountEquals(0)

        // Não busca a substring "signo": o subtítulo aprovado desta
        // entrega ("Usado pela numerologia do nome e pelo cálculo do
        // signo.") contém exatamente essa palavra e é texto legítimo do
        // RF-01.2 — colidir com ele reprovaria qualquer implementação
        // correta. Em vez disso, verifica a ausência dos 12 nomes de
        // signo (correspondência exata, não substring), que é o que de
        // fato apareceria no cartão "SEU SIGNO" de RF-01.5.
        nomesDosSignos.forEach { nomeDoSigno ->
            composeTestRule
                .onAllNodesWithText(nomeDoSigno, substring = false, ignoreCase = true)
                .assertCountEquals(0)
        }

        // Critério de aceite 16: o cartão passa a existir sempre — exatamente
        // um nó com o testTag reservado, mesmo sem signo calculável.
        composeTestRule
            .onAllNodes(hasTestTag(tagCartaoSigno))
            .assertCountEquals(1)

        // Critério de aceite 18: com o marcador neutro visível.
        composeTestRule.onNodeWithText(signoMarcadorNeutro).assertIsDisplayed()

        composeTestRule.onNodeWithText(ctaContinuar).assertIsEnabled()
    }

    @Test
    fun erroDeFormatoInvalidoExibeAMensagemCorrespondenteComTag() {
        // Fixture pré-RF-01.9 era "13081986" (verbatim, sem barras) — desde
        // que a máscara existe, esse valor nunca chega a este estado pelo
        // fluxo normal de digitação (a própria máscara insere as barras
        // antes de o ViewModel guardar). Este teste monta o UiState à mão,
        // sem passar pela máscara, então continua livre para expressar
        // FORMATO_INVALIDO com um valor alcançável de verdade: data
        // incompleta, ainda sem os 8 dígitos (7 dígitos: "12/08/198").
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "12/08/198",
                    erroNascimento = ErroDataNascimento.FORMATO_INVALIDO,
                ),
        )

        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithText(erroFormato).assertIsDisplayed()
    }

    @Test
    fun erroDeMesInvalidoExibeAMensagemCorrespondenteComTag() {
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/13/1978",
                    erroNascimento = ErroDataNascimento.MES_INVALIDO,
                ),
        )

        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithText(erroMes).assertIsDisplayed()
    }

    @Test
    fun erroDeDiaInexistenteExibeAMensagemCorrespondenteComTag() {
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "31/04/1978",
                    erroNascimento = ErroDataNascimento.DIA_INEXISTENTE,
                ),
        )

        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithText(erroDia).assertIsDisplayed()
    }

    @Test
    fun erroDeForaDoIntervaloExibeAMensagemCorrespondenteComTag() {
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "01/01/1899",
                    erroNascimento = ErroDataNascimento.FORA_DO_INTERVALO,
                ),
        )

        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithText(erroIntervalo).assertIsDisplayed()
    }

    @Test
    fun t1ErroDeMenorDeIdadeExibeAMensagemCorrespondenteComTag() {
        // RF-01.4 — "14/07/2020" é uma data formalmente válida
        // (nascimentoValido = true), reprovada só por idade.
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/07/2020",
                    nascimentoValido = true,
                    erroNascimento = ErroDataNascimento.MENOR_DE_IDADE,
                ),
        )

        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithText(erroMenorDeIdade).assertIsDisplayed()
    }

    @Test
    fun t2ComErroDeMenorDeIdadeContinuarSegueHabilitadoEOSignoAparece() {
        // Inverte a versão pré-RF-01.5 deste teste: RF-01.6 (travar avanço)
        // segue fora de escopo, mas RF-01.5 (cartão de signo) agora está
        // dentro. "14/07/2020" é uma data formalmente válida, formadora de
        // Câncer, reprovada só por idade — critério de aceite 12 e 19: os
        // dois julgamentos (idade e signo) são independentes, e a mensagem
        // de erro E o cartão com o signo aparecem ao mesmo tempo.
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/07/2020",
                    nascimentoValido = true,
                    erroNascimento = ErroDataNascimento.MENOR_DE_IDADE,
                    signo = Signo.CANCER,
                ),
        )

        composeTestRule.onNodeWithText(ctaContinuar).assertIsEnabled()

        // Critério de aceite 19: erro de idade e cartão de signo, juntos.
        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithText(erroMenorDeIdade).assertIsDisplayed()

        composeTestRule
            .onAllNodes(hasTestTag(tagCartaoSigno))
            .assertCountEquals(1)

        composeTestRule.onNodeWithText(nomeSignoCancer).assertIsDisplayed()
        composeTestRule.onNodeWithText(dezenasFormatadas(2, 7, 12, 20, 22)).assertIsDisplayed()
    }

    @Test
    fun comErroDeNascimentoContinuarSegueHabilitadoENenhumNomeDeSignoAparece() {
        // RF-01.6 (travar avanço) segue fora de escopo: mesmo com erro de
        // RF-01.3 exibido, o CTA "Continuar" segue habilitado. Data com mês
        // inválido não é calculável (signo == null): nenhum nome de signo
        // aparece — mas o cartão em si passou a existir sempre (RF-01.5),
        // com o marcador neutro.
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/13/1978",
                    erroNascimento = ErroDataNascimento.MES_INVALIDO,
                ),
        )

        composeTestRule.onNodeWithText(ctaContinuar).assertIsEnabled()

        nomesDosSignos.forEach { nomeDoSigno ->
            composeTestRule
                .onAllNodesWithText(nomeDoSigno, substring = false, ignoreCase = true)
                .assertCountEquals(0)
        }

        // Critério de aceite 16: o cartão continua existindo, agora com a
        // tag e o marcador neutro (não mais ausente).
        composeTestRule
            .onAllNodes(hasTestTag(tagCartaoSigno))
            .assertCountEquals(1)
        composeTestRule.onNodeWithText(signoMarcadorNeutro).assertIsDisplayed()
    }

    // --- RF-01.5 · seção "6.3 Tela" do critério de aceite (itens 16-22) ---

    @Test
    fun cartaoDeSignoExisteExatamenteUmaVezEmTodosOsEstados() {
        // Critério de aceite 16 — varre estado inicial, data válida com
        // signo, data inválida (sem signo) e menor de idade (com signo).
        //
        // Correção estrutural (`.claude/handoff.md`, achado do
        // `trevo-developer` em device real): `ComposeContentTestRule` só
        // aceita uma chamada de `setContent` por método de teste — chamar
        // `mostrarTelaIdentidade` (que chama `setContent`) dentro de um
        // `forEach` lança `IllegalStateException` na segunda iteração. Em
        // vez de içar o estado como parâmetro comum, este teste segue o
        // mesmo padrão já usado por
        // `mudancaExternaDeUiStateNascimentoReconciliaOCampoLocalComONovoValor`
        // (linhas 284–320): um único `setContent`, estado lido de dentro
        // do Composable via `mutableStateOf`, e cada mudança de estado
        // aplicada por `runOnUiThread` para recompor sem chamar
        // `setContent` de novo.
        val estados =
            listOf(
                IdentidadeUiState(),
                IdentidadeUiState(nascimento = "14/07/1978", nascimentoValido = true, signo = Signo.CANCER),
                IdentidadeUiState(nascimento = "14/13/1978", erroNascimento = ErroDataNascimento.MES_INVALIDO),
                IdentidadeUiState(
                    nascimento = "14/07/2020",
                    nascimentoValido = true,
                    erroNascimento = ErroDataNascimento.MENOR_DE_IDADE,
                    signo = Signo.CANCER,
                ),
            )

        val estado = mutableStateOf(estados.first())

        composeTestRule.setContent {
            TrevoTheme {
                TelaIdentidade(
                    uiState = estado.value,
                    onNomeChange = {},
                    onNascimentoChange = {},
                    onVoltarClick = {},
                    onContinuarClick = {},
                )
            }
        }

        estados.forEach { estadoDaVez ->
            composeTestRule.runOnUiThread {
                estado.value = estadoDaVez
            }
            composeTestRule.waitForIdle()

            composeTestRule
                .onAllNodes(hasTestTag(tagCartaoSigno))
                .assertCountEquals(1)
        }
    }

    @Test
    fun comSignoCalculavelOCartaoExibeONomeDoSignoEAsCincoDezenasRegidas() {
        // Critério de aceite 17 — dezenas de Câncer da tabela do plano
        // (2, 7, 12, 20, 22), no formato "02 · 07 · 12 · 20 · 22".
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/07/1978",
                    nascimentoValido = true,
                    signo = Signo.CANCER,
                ),
        )

        composeTestRule.onNodeWithTag(tagCartaoSigno).assertIsDisplayed()
        composeTestRule.onNodeWithText(nomeSignoCancer).assertIsDisplayed()
        composeTestRule.onNodeWithText(dezenasFormatadas(2, 7, 12, 20, 22)).assertIsDisplayed()
    }

    @Test
    fun comSignoNuloOCartaoExibeOMarcadorNeutroENenhumDosDozeNomesDeSignoApareceNaArvore() {
        // Critério de aceite 18.
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/13/1978",
                    erroNascimento = ErroDataNascimento.MES_INVALIDO,
                ),
        )

        composeTestRule.onNodeWithTag(tagCartaoSigno).assertIsDisplayed()
        composeTestRule.onNodeWithText(signoMarcadorNeutro).assertIsDisplayed()

        nomesDosSignos.forEach { nomeDoSigno ->
            composeTestRule
                .onAllNodesWithText(nomeDoSigno, substring = false, ignoreCase = true)
                .assertCountEquals(0)
        }
    }

    @Test
    fun estadoDeMenorDeIdadeExibeErroDeNascimentoECartaoComOSignoAoMesmoTempo() {
        // Critério de aceite 19, isolado por nome (também coberto, junto com
        // o CTA, por t2ComErroDeMenorDeIdadeContinuarSegueHabilitadoEOSignoAparece).
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/07/2020",
                    nascimentoValido = true,
                    erroNascimento = ErroDataNascimento.MENOR_DE_IDADE,
                    signo = Signo.CANCER,
                ),
        )

        composeTestRule.onNodeWithTag(TAG_ERRO_NASCIMENTO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(tagCartaoSigno).assertIsDisplayed()
        composeTestRule.onNodeWithText(nomeSignoCancer).assertIsDisplayed()
    }

    @Test
    fun oCartaoExpoeContentDescriptionDistintaEntreOEstadoComSignoEOEstadoNeutro() {
        // Critério de aceite 20. Texto com signo literal do plano
        // `.claude/handoff.md` (decisão D5): "Seu signo: Câncer. Dezenas
        // regidas: 2, 7, 12, 20 e 22." O estado neutro usa o recurso
        // `identidade_signo_descricao_neutro` diretamente.
        val descricaoComSignoEsperada =
            signoDescricaoFormato
                .replace("%1\$s", nomeSignoCancer)
                .replace("%2\$s", "2, 7, 12, 20 e 22")
        assertNotEquals(descricaoComSignoEsperada, signoDescricaoNeutro)

        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/07/1978",
                    nascimentoValido = true,
                    signo = Signo.CANCER,
                ),
        )
        composeTestRule
            .onNodeWithTag(tagCartaoSigno)
            .assertContentDescriptionEquals(descricaoComSignoEsperada)
    }

    @Test
    fun oCartaoExpoeAContentDescriptionNeutraQuandoOSignoENulo() {
        // Segunda metade do critério de aceite 20 — estado neutro isolado
        // por nome, para não depender da ordem de setContent do teste
        // anterior.
        mostrarTelaIdentidade(
            uiState =
                IdentidadeUiState(
                    nascimento = "14/13/1978",
                    erroNascimento = ErroDataNascimento.MES_INVALIDO,
                ),
        )
        composeTestRule
            .onNodeWithTag(tagCartaoSigno)
            .assertContentDescriptionEquals(signoDescricaoNeutro)
    }

    @Test
    fun nenhumaStringDaTelaDeIdentidadePrometeAumentoDeChance() {
        val stringsDaTela =
            mapOf(
                "identidade_titulo" to titulo,
                "identidade_subtitulo" to subtitulo,
                "identidade_label_nome" to labelNome,
                "identidade_placeholder_nome" to placeholderNome,
                "identidade_label_nascimento" to labelNascimento,
                "identidade_placeholder_nascimento" to placeholderNascimento,
                "identidade_cta_voltar" to ctaVoltar,
                "identidade_cta_continuar" to ctaContinuar,
                "identidade_progresso_descricao" to progressoDescricao,
                "identidade_erro_formato" to erroFormato,
                "identidade_erro_mes" to erroMes,
                "identidade_erro_dia" to erroDia,
                "identidade_erro_intervalo" to erroIntervalo,
                "identidade_erro_menor_idade" to erroMenorDeIdade,
                // RF-01.5 — as 17 strings novas do cartão "SEU SIGNO"
                // (plano `.claude/handoff.md`, decisão D6): regra
                // inviolável 2 exige que nenhuma delas prometa aumento de
                // chance, mesmo apresentando uma crença popular.
                "identidade_signo_rotulo" to signoRotulo,
                "identidade_signo_marcador_neutro" to signoMarcadorNeutro,
                "identidade_signo_separador_dezenas" to signoSeparadorDezenas,
                "identidade_signo_descricao" to signoDescricaoFormato,
                "identidade_signo_descricao_neutro" to signoDescricaoNeutro,
                "signo_capricornio" to nomeSignoCapricornio,
                "signo_aquario" to nomeSignoAquario,
                "signo_peixes" to nomeSignoPeixes,
                "signo_aries" to nomeSignoAries,
                "signo_touro" to nomeSignoTouro,
                "signo_gemeos" to nomeSignoGemeos,
                "signo_cancer" to nomeSignoCancer,
                "signo_leao" to nomeSignoLeao,
                "signo_virgem" to nomeSignoVirgem,
                "signo_libra" to nomeSignoLibra,
                "signo_escorpiao" to nomeSignoEscorpiao,
                "signo_sagitario" to nomeSignoSagitario,
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
}
