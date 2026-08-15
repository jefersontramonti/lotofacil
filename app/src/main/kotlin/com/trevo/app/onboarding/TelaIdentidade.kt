package com.trevo.app.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.engine.identidade.EdicaoDataNascimento
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.aplicarMascaraDataNascimento
import com.trevo.core.ui.BotaoPrimario
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import com.trevo.core.ui.NocturneText

const val TAG_CAMPO_NOME = "campo_nome"
const val TAG_CAMPO_NASCIMENTO = "campo_nascimento"
const val TAG_ERRO_NASCIMENTO = "erro_nascimento"
const val TAG_CARTAO_SIGNO = "cartao_signo"

@Composable
fun TelaIdentidade(
    uiState: IdentidadeUiState,
    onNomeChange: (String) -> Unit,
    onNascimentoChange: (String) -> Unit,
    onVoltarClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.identidade_progresso_descricao),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(id = R.string.identidade_titulo),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(id = R.string.identidade_subtitulo),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedTextField(
                value = uiState.nome,
                onValueChange = onNomeChange,
                modifier = Modifier.fillMaxWidth().testTag(TAG_CAMPO_NOME),
                label = { Text(text = stringResource(id = R.string.identidade_label_nome)) },
                placeholder = { Text(text = stringResource(id = R.string.identidade_placeholder_nome)) },
                singleLine = true,
            )
            var campoNascimento by
                rememberSaveable(stateSaver = TextFieldValue.Saver) {
                    mutableStateOf(textoNoFim(uiState.nascimento))
                }
            // `ultimoNascimentoEmitido` guarda o último valor que esta própria tela
            // mandou via `onNascimentoChange` — não o texto local, nem o `uiState`.
            // `LaunchedEffect(uiState.nascimento)` dispara em toda mudança desse valor,
            // inclusive o eco (síncrono ou não) do que a tela acabou de emitir; comparar
            // contra `ultimoNascimentoEmitido` (atualizado no mesmo evento que emite,
            // antes de qualquer recomposição) é o que distingue esse eco de uma mudança
            // externa de verdade (restauração de processo, RF-07.1) — só a segunda
            // ressincroniza `campoNascimento`.
            var ultimoNascimentoEmitido by rememberSaveable { mutableStateOf(uiState.nascimento) }
            LaunchedEffect(uiState.nascimento) {
                if (uiState.nascimento != ultimoNascimentoEmitido) {
                    campoNascimento = textoNoFim(uiState.nascimento)
                    ultimoNascimentoEmitido = uiState.nascimento
                }
            }
            OutlinedTextField(
                value = campoNascimento,
                onValueChange = { novoValor ->
                    val edicao =
                        aplicarMascaraDataNascimento(
                            campoNascimento.text,
                            EdicaoDataNascimento(novoValor.text, novoValor.selection.start),
                        )
                    campoNascimento = TextFieldValue(text = edicao.texto, selection = TextRange(edicao.cursor))
                    ultimoNascimentoEmitido = edicao.texto
                    onNascimentoChange(edicao.texto)
                },
                modifier = Modifier.fillMaxWidth().testTag(TAG_CAMPO_NASCIMENTO),
                label = { Text(text = stringResource(id = R.string.identidade_label_nascimento)) },
                placeholder = { Text(text = stringResource(id = R.string.identidade_placeholder_nascimento)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.erroNascimento != null,
                supportingText =
                    uiState.erroNascimento?.let { erro ->
                        {
                            Row(
                                modifier =
                                    Modifier
                                        .testTag(TAG_ERRO_NASCIMENTO)
                                        .semantics(mergeDescendants = true) {},
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.identidade_erro_indicador),
                                    modifier = Modifier.clearAndSetSemantics {},
                                )
                                Text(text = stringResource(id = mensagemDe(erro)))
                            }
                        }
                    },
            )
            CartaoSigno(signo = uiState.signo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BotaoPrimario(
                    texto = stringResource(id = R.string.identidade_cta_voltar),
                    onClick = onVoltarClick,
                    modifier = Modifier.weight(1f),
                )
                BotaoPrimario(
                    texto = stringResource(id = R.string.identidade_cta_continuar),
                    onClick = onContinuarClick,
                    modifier = Modifier.weight(1f),
                    // RF-01.6: trava o avanço enquanto houver erro de validação.
                    // `nascimentoValido` sozinho não basta — ele continua `true`
                    // sob MENOR_DE_IDADE (RF-01.4 é um julgamento independente
                    // sobre a mesma data válida), então checar `erroNascimento
                    // == null` também é o que garante que RF-01.4 barra o avanço.
                    enabled = uiState.nascimentoValido && uiState.erroNascimento == null,
                )
            }
        }
    }
}

private fun textoNoFim(texto: String): TextFieldValue =
    TextFieldValue(text = texto, selection = TextRange(texto.length))

@StringRes
private fun mensagemDe(erro: ErroDataNascimento): Int =
    when (erro) {
        ErroDataNascimento.VAZIO -> error("VAZIO não gera mensagem na tela — campo intocado é silencioso")
        ErroDataNascimento.FORMATO_INVALIDO -> R.string.identidade_erro_formato
        ErroDataNascimento.MES_INVALIDO -> R.string.identidade_erro_mes
        ErroDataNascimento.DIA_INEXISTENTE -> R.string.identidade_erro_dia
        ErroDataNascimento.FORA_DO_INTERVALO -> R.string.identidade_erro_intervalo
        ErroDataNascimento.MENOR_DE_IDADE -> R.string.identidade_erro_menor_idade
    }

// Cartão "SEU SIGNO" — sempre visível (wireframe 1b), inclusive para menor
// de idade: signo é sobre a data, não sobre a autorização de cadastro
// (RF-01.4/RF-01.6 são julgamentos independentes). Sem signo calculável, o
// cartão mostra o marcador neutro — nunca um signo padrão.
@Composable
private fun CartaoSigno(
    signo: Signo?,
    modifier: Modifier = Modifier,
) {
    val marcadorNeutro = stringResource(id = R.string.identidade_signo_marcador_neutro)
    val separadorDezenas = stringResource(id = R.string.identidade_signo_separador_dezenas)
    val nomeSigno = signo?.let { stringResource(id = nomeDe(it)) } ?: marcadorNeutro
    val dezenasVisiveis = signo?.let { dezenasFormatadasParaExibicao(it.dezenas, separadorDezenas) } ?: marcadorNeutro
    val descricaoCartao =
        signo?.let {
            stringResource(
                id = R.string.identidade_signo_descricao,
                stringResource(id = nomeDe(it)),
                dezenasFormatadasParaFala(it.dezenas),
            )
        } ?: stringResource(id = R.string.identidade_signo_descricao_neutro)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
                .testTag(TAG_CARTAO_SIGNO)
                .semantics(mergeDescendants = true) {
                    contentDescription = descricaoCartao
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (signo != null) {
            Image(
                painter = painterResource(id = iconeDe(signo)),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .border(border = BorderStroke(1.dp, NocturneAccent), shape = CircleShape),
            )
        }
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = stringResource(id = R.string.identidade_signo_rotulo),
                style = MaterialTheme.typography.labelLarge,
                color = NocturneAccent,
            )
            Text(
                text = nomeSigno,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = dezenasVisiveis,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            // Contraste medido (não presumido): NocturneText a 68% de opacidade
            // composto sobre NocturneSurface dá 6,58:1, acima do mínimo de
            // 4,5:1 (CLAUDE.md §6). Mesma opacidade de referência do protótipo.
            color = NocturneText.copy(alpha = 0.68f),
            textAlign = TextAlign.End,
        )
    }
}

private fun dezenasFormatadasParaExibicao(
    dezenas: List<Int>,
    separador: String,
): String = dezenas.joinToString(separator = separador) { it.toString().padStart(2, '0') }

private fun dezenasFormatadasParaFala(dezenas: List<Int>): String =
    if (dezenas.size <= 1) {
        dezenas.joinToString()
    } else {
        dezenas.dropLast(1).joinToString(", ") + " e " + dezenas.last()
    }

@StringRes
private fun nomeDe(signo: Signo): Int =
    when (signo) {
        Signo.CAPRICORNIO -> R.string.signo_capricornio
        Signo.AQUARIO -> R.string.signo_aquario
        Signo.PEIXES -> R.string.signo_peixes
        Signo.ARIES -> R.string.signo_aries
        Signo.TOURO -> R.string.signo_touro
        Signo.GEMEOS -> R.string.signo_gemeos
        Signo.CANCER -> R.string.signo_cancer
        Signo.LEAO -> R.string.signo_leao
        Signo.VIRGEM -> R.string.signo_virgem
        Signo.LIBRA -> R.string.signo_libra
        Signo.ESCORPIAO -> R.string.signo_escorpiao
        Signo.SAGITARIO -> R.string.signo_sagitario
    }

@DrawableRes
private fun iconeDe(signo: Signo): Int =
    when (signo) {
        Signo.CAPRICORNIO -> R.drawable.ic_signo_capricornio
        Signo.AQUARIO -> R.drawable.ic_signo_aquario
        Signo.PEIXES -> R.drawable.ic_signo_peixes
        Signo.ARIES -> R.drawable.ic_signo_aries
        Signo.TOURO -> R.drawable.ic_signo_touro
        Signo.GEMEOS -> R.drawable.ic_signo_gemeos
        Signo.CANCER -> R.drawable.ic_signo_cancer
        Signo.LEAO -> R.drawable.ic_signo_leao
        Signo.VIRGEM -> R.drawable.ic_signo_virgem
        Signo.LIBRA -> R.drawable.ic_signo_libra
        Signo.ESCORPIAO -> R.drawable.ic_signo_escorpiao
        Signo.SAGITARIO -> R.drawable.ic_signo_sagitario
    }
