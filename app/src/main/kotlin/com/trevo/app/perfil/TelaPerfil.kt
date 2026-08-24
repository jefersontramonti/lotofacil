package com.trevo.app.perfil

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.app.assinatura.nomeDoPlano
import com.trevo.core.engine.identidade.EdicaoDataNascimento
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.aplicarMascaraDataNascimento
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneAccentMuted
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import com.trevo.core.ui.NocturneText
import java.time.LocalTime
import java.time.format.DateTimeFormatter

const val TAG_CAMPO_NOME_PERFIL = "perfil_campo_nome"
const val TAG_CAMPO_NASCIMENTO_PERFIL = "perfil_campo_nascimento"
const val TAG_CARTAO_CRENCAS_PERFIL = "perfil_cartao_crencas"
const val TAG_SWITCH_LEMBRETE = "perfil_switch_lembrete"
const val TAG_SWITCH_RESULTADO = "perfil_switch_resultado"
const val TAG_CHIP_HORARIO_ATUAL = "perfil_chip_horario_atual"
const val TAG_CARTAO_ASSINATURA = "perfil_cartao_assinatura"
const val TAG_CARTAO_EXCLUIR_DADOS = "perfil_cartao_excluir_dados"
const val TAG_BOTAO_CONFIRMAR_EXCLUSAO_DADOS = "perfil_confirmar_exclusao_dados"

private val HORARIOS_SUGERIDOS = listOf(LocalTime.of(17, 0), LocalTime.of(18, 0), LocalTime.of(18, 30))
private val FORMATO_HORARIO = DateTimeFormatter.ofPattern("HH:mm")

fun tagChipHorario(horario: LocalTime): String = "perfil_chip_horario_${FORMATO_HORARIO.format(horario)}"

@Composable
fun TelaPerfil(
    uiState: PerfilUiState,
    onNomeChange: (String) -> Unit,
    onNascimentoChange: (String) -> Unit,
    onCrencasClick: () -> Unit,
    onAlternarLembreteFechamento: (Boolean) -> Unit,
    onEscolherHorarioLembrete: (LocalTime) -> Unit,
    onAlternarNotificacaoResultado: (Boolean) -> Unit,
    onAssinaturaClick: () -> Unit = {},
    onConfirmarExclusaoDeDadosClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var mostrarDialogoDeHorario by remember { mutableStateOf(false) }
    var mostrarDialogoDeExclusaoDeDados by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    // A barra de navegação inferior já cobre o inset de
                    // baixo (ver TrevoNavHost) — não pedir de novo aqui.
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    ).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = stringResource(id = R.string.perfil_titulo), style = MaterialTheme.typography.headlineSmall)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker(texto = stringResource(id = R.string.perfil_label_nome))
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = onNomeChange,
                    modifier = Modifier.fillMaxWidth().testTag(TAG_CAMPO_NOME_PERFIL),
                    singleLine = true,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker(texto = stringResource(id = R.string.perfil_label_nascimento))
                CampoNascimento(
                    valor = uiState.nascimento,
                    onValorChange = onNascimentoChange,
                    erro = uiState.erroNascimento,
                )
                Text(text = notaDeSigno(uiState.signo), style = MaterialTheme.typography.bodySmall)
            }

            CartaoDeNavegacao(
                icone = stringResource(id = R.string.perfil_crencas_icone_indicador),
                titulo = stringResource(id = R.string.perfil_crencas_titulo),
                descricao =
                    pluralStringResource(
                        id = R.plurals.perfil_crencas_subtitulo,
                        count = uiState.quantidadeDeCrencasAtivas,
                        uiState.quantidadeDeCrencasAtivas,
                    ),
                onClick = onCrencasClick,
                testTag = TAG_CARTAO_CRENCAS_PERFIL,
            )

            Kicker(texto = stringResource(id = R.string.perfil_notificacoes_titulo))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                        .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.perfil_lembrete_titulo),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(id = R.string.perfil_lembrete_descricao),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    SwitchDeNotificacao(
                        marcado = uiState.lembreteFechamentoAtivo,
                        onMarcadoChange = onAlternarLembreteFechamento,
                        descricao = stringResource(id = R.string.perfil_lembrete_switch_descricao),
                        testTag = TAG_SWITCH_LEMBRETE,
                    )
                }
                // FlowRow, não Row: um Row comum mede os chips em sequência e,
                // com fonte grande o bastante, o último pode sobrar só alguns dp
                // de largura e quebrar letra por letra em vez de ir pra linha de
                // baixo (achado real a 200% de fonte, RNF-03.3 — mesma causa do
                // "seg." na Home).
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ChipHorarioAtual(
                        horario = uiState.horarioLembreteFechamento,
                        onClick = { mostrarDialogoDeHorario = true },
                    )
                    HORARIOS_SUGERIDOS.forEach { horario ->
                        ChipHorarioSugerido(
                            horario = horario,
                            selecionado = horario == uiState.horarioLembreteFechamento,
                            onClick = { onEscolherHorarioLembrete(horario) },
                        )
                    }
                }
                Text(
                    text =
                        stringResource(
                            id = R.string.perfil_lembrete_aviso_horario,
                            FORMATO_HORARIO.format(uiState.horarioLembreteFechamento),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (uiState.alertaHorarioAposFechamento) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.semantics(mergeDescendants = true) {},
                    ) {
                        Text(
                            text = stringResource(id = R.string.perfil_erro_indicador),
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                        Text(
                            text = stringResource(id = R.string.perfil_lembrete_alerta_apos_fechamento),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                        .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.perfil_resultado_titulo),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.perfil_resultado_descricao),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                SwitchDeNotificacao(
                    marcado = uiState.notificacaoResultadoAtiva,
                    onMarcadoChange = onAlternarNotificacaoResultado,
                    descricao = stringResource(id = R.string.perfil_resultado_switch_descricao),
                    testTag = TAG_SWITCH_RESULTADO,
                )
            }

            Kicker(texto = stringResource(id = R.string.perfil_assinatura_titulo))

            // RF-07.8/RF-09.6 — estado real, derivado do Billing
            // (AssinaturaRepository). Grátis abre o paywall; Pro abre a
            // gestão da assinatura na Play Store (RF-09.7 é restaurar, não
            // gerenciar — ações diferentes, ambas fora do app).
            val produtoId = uiState.productIdDaAssinatura
            val tituloAssinaturaId =
                if (uiState.isPro) R.string.perfil_assinatura_pro_titulo else R.string.perfil_assinatura_gratuito_titulo
            CartaoDeNavegacao(
                icone = null,
                titulo = stringResource(id = tituloAssinaturaId),
                descricao =
                    if (uiState.isPro && produtoId != null) {
                        stringResource(
                            id = R.string.perfil_assinatura_pro_descricao,
                            stringResource(id = nomeDoPlano(produtoId)),
                        )
                    } else {
                        stringResource(id = R.string.perfil_assinatura_gratuito_descricao)
                    },
                onClick = onAssinaturaClick,
                testTag = TAG_CARTAO_ASSINATURA,
            )

            Kicker(texto = stringResource(id = R.string.perfil_dados_titulo))

            CartaoDeNavegacao(
                icone = null,
                titulo = stringResource(id = R.string.perfil_excluir_dados_titulo),
                descricao = stringResource(id = R.string.perfil_excluir_dados_descricao),
                onClick = { mostrarDialogoDeExclusaoDeDados = true },
                testTag = TAG_CARTAO_EXCLUIR_DADOS,
            )
        }
    }

    if (mostrarDialogoDeHorario) {
        DialogoDeHorario(
            horarioInicial = uiState.horarioLembreteFechamento,
            onConfirmar = { horario ->
                onEscolherHorarioLembrete(horario)
                mostrarDialogoDeHorario = false
            },
            onCancelar = { mostrarDialogoDeHorario = false },
        )
    }

    if (mostrarDialogoDeExclusaoDeDados) {
        DialogoConfirmarExclusaoDeDados(
            onConfirmarClick = {
                mostrarDialogoDeExclusaoDeDados = false
                onConfirmarExclusaoDeDadosClick()
            },
            onCancelarClick = { mostrarDialogoDeExclusaoDeDados = false },
        )
    }
}

@Composable
private fun DialogoConfirmarExclusaoDeDados(
    onConfirmarClick: () -> Unit,
    onCancelarClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelarClick,
        title = { Text(text = stringResource(id = R.string.perfil_excluir_dados_confirmar_titulo)) },
        text = { Text(text = stringResource(id = R.string.perfil_excluir_dados_confirmar_mensagem)) },
        confirmButton = {
            TextButton(
                onClick = onConfirmarClick,
                modifier = Modifier.testTag(TAG_BOTAO_CONFIRMAR_EXCLUSAO_DADOS),
            ) {
                Text(text = stringResource(id = R.string.perfil_excluir_dados_confirmar_cta))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelarClick) {
                Text(text = stringResource(id = R.string.perfil_excluir_dados_cancelar_cta))
            }
        },
    )
}

@Composable
private fun Kicker(
    texto: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = NocturneText.copy(alpha = 0.6f),
    )
}

@Composable
private fun CartaoDeNavegacao(
    icone: String?,
    titulo: String,
    descricao: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) {}
                .padding(12.dp)
                .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icone != null) {
            Text(text = icone, color = NocturneAccent, modifier = Modifier.clearAndSetSemantics {})
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = descricao, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = stringResource(id = R.string.perfil_chevron_indicador),
            color = NocturneText.copy(alpha = 0.6f),
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun SwitchDeNotificacao(
    marcado: Boolean,
    onMarcadoChange: (Boolean) -> Unit,
    descricao: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = marcado,
        onCheckedChange = onMarcadoChange,
        modifier =
            modifier
                .testTag(testTag)
                .semantics { contentDescription = descricao },
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = NocturneAccent,
                checkedTrackColor = NocturneAccentMuted,
                checkedBorderColor = NocturneAccent,
                uncheckedThumbColor = NocturneText,
                uncheckedTrackColor = NocturneSurface,
                uncheckedBorderColor = NocturneOutline,
            ),
    )
}

@Composable
private fun ChipHorarioAtual(
    horario: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricao = stringResource(id = R.string.perfil_horario_customizado_descricao, FORMATO_HORARIO.format(horario))
    Box(
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .border(border = BorderStroke(1.dp, NocturneAccent), shape = RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = descricao }
                .testTag(TAG_CHIP_HORARIO_ATUAL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${FORMATO_HORARIO.format(horario)} ⏱",
            style = MaterialTheme.typography.bodySmall,
            color = NocturneAccent,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@Composable
private fun ChipHorarioSugerido(
    horario: LocalTime,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cor = if (selecionado) NocturneAccent else NocturneOutline
    Box(
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .border(border = BorderStroke(1.dp, cor), shape = RoundedCornerShape(8.dp))
                .selectable(selected = selecionado, onClick = onClick, role = Role.RadioButton)
                .testTag(tagChipHorario(horario)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = FORMATO_HORARIO.format(horario),
            style = MaterialTheme.typography.bodySmall,
            // RNF: marcação nunca só por cor — o preset escolhido também
            // engrossa o peso da fonte, não só a cor da borda.
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoDeHorario(
    horarioInicial: LocalTime,
    onConfirmar: (LocalTime) -> Unit,
    onCancelar: () -> Unit,
) {
    val estado =
        rememberTimePickerState(
            initialHour = horarioInicial.hour,
            initialMinute = horarioInicial.minute,
            is24Hour = true,
        )
    AlertDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(onClick = { onConfirmar(LocalTime.of(estado.hour, estado.minute)) }) {
                Text(text = stringResource(id = R.string.perfil_horario_dialogo_confirmar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = stringResource(id = R.string.perfil_horario_dialogo_cancelar))
            }
        },
        title = { Text(text = stringResource(id = R.string.perfil_horario_dialogo_titulo)) },
        text = { TimePicker(state = estado) },
    )
}

@Composable
private fun CampoNascimento(
    valor: String,
    onValorChange: (String) -> Unit,
    erro: ErroDataNascimento?,
    modifier: Modifier = Modifier,
) {
    var campoLocal by
        rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(text = valor, selection = TextRange(valor.length)))
        }
    // Mesmo padrão de TelaIdentidade: o texto local só ressincroniza com o
    // uiState quando a mudança não veio do próprio campo (restauração de
    // processo), nunca a cada eco do que ele mesmo acabou de emitir.
    var ultimoValorEmitido by rememberSaveable { mutableStateOf(valor) }
    LaunchedEffect(valor) {
        if (valor != ultimoValorEmitido) {
            campoLocal = TextFieldValue(text = valor, selection = TextRange(valor.length))
            ultimoValorEmitido = valor
        }
    }

    OutlinedTextField(
        value = campoLocal,
        onValueChange = { novoValor ->
            val edicao =
                aplicarMascaraDataNascimento(
                    campoLocal.text,
                    EdicaoDataNascimento(novoValor.text, novoValor.selection.start),
                )
            campoLocal = TextFieldValue(text = edicao.texto, selection = TextRange(edicao.cursor))
            ultimoValorEmitido = edicao.texto
            onValorChange(edicao.texto)
        },
        modifier = modifier.fillMaxWidth().testTag(TAG_CAMPO_NASCIMENTO_PERFIL),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = erro != null,
        supportingText =
            erro?.let {
                {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(id = R.string.perfil_erro_indicador),
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                        Text(text = stringResource(id = mensagemDe(it)))
                    }
                }
            },
    )
}

@Composable
private fun notaDeSigno(signo: Signo?): String {
    val separador = stringResource(id = R.string.identidade_signo_separador_dezenas)
    return signo?.let {
        stringResource(
            id = R.string.perfil_signo_nota,
            stringResource(id = nomeDeSigno(it)),
            it.dezenas.joinToString(separator = separador) { dezena -> dezena.toString().padStart(2, '0') },
        )
    } ?: stringResource(id = R.string.perfil_signo_nota_neutro)
}

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

@StringRes
private fun nomeDeSigno(signo: Signo): Int =
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
