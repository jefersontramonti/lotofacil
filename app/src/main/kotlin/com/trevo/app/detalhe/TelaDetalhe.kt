package com.trevo.app.detalhe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.app.ritual.emojiDoAmuleto
import com.trevo.app.ritual.fraseDaEscolha
import com.trevo.app.ritual.nomeDoAmuleto
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import com.trevo.core.ui.BotaoPrimario
import com.trevo.core.ui.BotaoVoltar
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import java.text.NumberFormat
import java.util.Locale

fun tagBotaoExcluirDetalhe(): String = "detalhe_excluir"

fun tagBotaoRefazer(): String = "detalhe_refazer"

fun tagBotaoSalvarEdicao(): String = "detalhe_salvar_edicao"

fun tagBotaoCompartilhar(): String = "detalhe_compartilhar"

fun tagBotaoEnviarWhatsApp(): String = "detalhe_compartilhar_whatsapp"

fun tagBotaoCopiarTexto(): String = "detalhe_compartilhar_copiar"

@Composable
fun TelaDetalhe(
    uiState: DetalheUiState,
    onVoltarClick: () -> Unit,
    onEditarClick: () -> Unit,
    onRefazerClick: () -> Unit,
    onExcluirClick: () -> Unit,
    onConfirmarExclusaoClick: () -> Unit,
    onCancelarExclusaoClick: () -> Unit,
    onDezenaClick: (Int) -> Unit,
    onAlternarGuardarFixasClick: () -> Unit,
    onCancelarEdicaoClick: () -> Unit,
    onSalvarEdicaoClick: () -> Unit,
    onLimparFixasClick: () -> Unit,
    onVerDesdobramentosClick: () -> Unit,
    onCompartilharClick: () -> Unit,
    onFecharCompartilharClick: () -> Unit,
    onEnviarWhatsAppClick: (String) -> Unit,
    onCopiarTextoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    // A barra de navegação inferior (fora desta Composable,
                    // ver TrevoNavHost) já cobre o inset de baixo — pedir de
                    // novo aqui dobraria o espaço reservado.
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    ).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Cabecalho(
                uiState = uiState,
                onVoltarClick = onVoltarClick,
                onRefazerClick = onRefazerClick,
                onExcluirClick = onExcluirClick,
                onCompartilharClick = onCompartilharClick,
            )
            if (!uiState.palpiteExiste) {
                Text(
                    text = stringResource(id = R.string.detalhe_nao_encontrado),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                GradeDeDezenas(
                    dezenasMarcadas = if (uiState.modoEdicao) uiState.dezenasEmEdicao else uiState.dezenas.toSet(),
                    dezenasFixas = uiState.dezenasFixas.toSet(),
                    onDezenaClick = if (uiState.modoEdicao) onDezenaClick else null,
                )
                if (uiState.modoEdicao) {
                    SecaoEdicao(
                        uiState = uiState,
                        onAlternarGuardarFixasClick = onAlternarGuardarFixasClick,
                        onCancelarClick = onCancelarEdicaoClick,
                        onSalvarClick = onSalvarEdicaoClick,
                    )
                } else {
                    SecaoVisualizacao(
                        uiState = uiState,
                        onEditarClick = onEditarClick,
                        onLimparFixasClick = onLimparFixasClick,
                        onVerDesdobramentosClick = onVerDesdobramentosClick,
                    )
                }
            }
        }
    }

    if (uiState.palpiteParaConfirmarExclusao) {
        AlertDialog(
            onDismissRequest = onCancelarExclusaoClick,
            title = { Text(text = stringResource(id = R.string.home_excluir_confirmar_titulo)) },
            text = { Text(text = stringResource(id = R.string.home_excluir_confirmar_mensagem)) },
            confirmButton = {
                TextButton(onClick = onConfirmarExclusaoClick) {
                    Text(text = stringResource(id = R.string.home_excluir_confirmar_cta))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarExclusaoClick) {
                    Text(text = stringResource(id = R.string.home_excluir_cancelar_cta))
                }
            },
        )
    }

    if (uiState.compartilhando) {
        FolhaDeCompartilhamento(
            uiState = uiState,
            onFecharClick = onFecharCompartilharClick,
            onEnviarWhatsAppClick = onEnviarWhatsAppClick,
            onCopiarTextoClick = onCopiarTextoClick,
        )
    }
}

@Composable
private fun Cabecalho(
    uiState: DetalheUiState,
    onVoltarClick: () -> Unit,
    onRefazerClick: () -> Unit,
    onExcluirClick: () -> Unit,
    onCompartilharClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoVoltar = stringResource(id = R.string.detalhe_voltar_descricao)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BotaoVoltar(onClick = onVoltarClick, descricao = descricaoVoltar)
        Text(
            text = stringResource(id = R.string.home_palpite_rotulo, uiState.numeroDoDia),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
        if (uiState.modoEdicao) {
            Text(text = stringResource(id = R.string.detalhe_editando_tag), style = MaterialTheme.typography.labelSmall)
        } else if (uiState.palpiteExiste) {
            val descricaoCompartilhar = stringResource(id = R.string.detalhe_compartilhar_icone_descricao)
            val descricaoRefazer = stringResource(id = R.string.detalhe_refazer_descricao)
            val descricaoExcluir = stringResource(id = R.string.detalhe_excluir_icone_descricao)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "📤",
                    modifier =
                        Modifier
                            .clickable(role = Role.Button, onClick = onCompartilharClick)
                            .semantics { contentDescription = descricaoCompartilhar }
                            .testTag(tagBotaoCompartilhar()),
                )
                Text(
                    text = "↻",
                    modifier =
                        Modifier
                            .clickable(role = Role.Button, onClick = onRefazerClick)
                            .semantics { contentDescription = descricaoRefazer }
                            .testTag(tagBotaoRefazer()),
                )
                Text(
                    text = "🗑",
                    modifier =
                        Modifier
                            .clickable(role = Role.Button, onClick = onExcluirClick)
                            .semantics { contentDescription = descricaoExcluir }
                            .testTag(tagBotaoExcluirDetalhe()),
                )
            }
        }
    }
}

// RF-08.1/08.2 — texto pronto pra envio, montado a partir de string.xml
// (nunca literal), com o número do concurso omitido quando ainda
// desconhecido (ver nota em DetalheUiState.numeroDoConcurso).
@Composable
private fun mensagemDeCompartilhamento(uiState: DetalheUiState): String {
    val dezenasTexto = uiState.dezenas.joinToString(" · ") { "%02d".format(it) }
    val crencasTexto =
        pluralStringResource(
            id = R.plurals.detalhe_compartilhar_crencas,
            count = uiState.origens.size,
            uiState.origens.size,
        )
    val numeroDoConcurso = uiState.numeroDoConcurso
    return if (numeroDoConcurso != null) {
        stringResource(
            id = R.string.detalhe_compartilhar_mensagem_com_concurso,
            numeroDoConcurso,
            dezenasTexto,
            crencasTexto,
        )
    } else {
        stringResource(id = R.string.detalhe_compartilhar_mensagem_sem_concurso, dezenasTexto, crencasTexto)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolhaDeCompartilhamento(
    uiState: DetalheUiState,
    onFecharClick: () -> Unit,
    onEnviarWhatsAppClick: (String) -> Unit,
    onCopiarTextoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mensagem = mensagemDeCompartilhamento(uiState)
    ModalBottomSheet(onDismissRequest = onFecharClick, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.detalhe_compartilhar_titulo),
                style = MaterialTheme.typography.titleLarge,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                        .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
            ) {
                Text(text = mensagem, style = MaterialTheme.typography.bodySmall)
            }
            BotaoPrimario(
                texto = stringResource(id = R.string.detalhe_compartilhar_whatsapp_cta),
                onClick = { onEnviarWhatsAppClick(mensagem) },
                modifier = Modifier.fillMaxWidth().testTag(tagBotaoEnviarWhatsApp()),
            )
            Text(
                text = stringResource(id = R.string.detalhe_compartilhar_copiar_cta),
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = { onCopiarTextoClick(mensagem) })
                        .testTag(tagBotaoCopiarTexto())
                        .padding(vertical = 12.dp),
            )
            if (uiState.copiado) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                            .border(border = BorderStroke(1.dp, NocturneAccent), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.detalhe_compartilhar_copiado_confirmacao),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.detalhe_compartilhar_fechar_cta),
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onFecharClick)
                        .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun SecaoVisualizacao(
    uiState: DetalheUiState,
    onEditarClick: () -> Unit,
    onLimparFixasClick: () -> Unit,
    onVerDesdobramentosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BotaoPrimario(texto = stringResource(id = R.string.detalhe_editar_cta), onClick = onEditarClick)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CaixaDeDestaque(
                titulo = stringResource(id = R.string.detalhe_forca_titulo),
                valor = uiState.forca.toString(),
                modifier = Modifier.weight(1f),
            )
            CaixaDeDestaque(
                titulo = stringResource(id = R.string.detalhe_chance_titulo),
                valor = stringResource(id = R.string.detalhe_chance_valor, formatarInteiro(uiState.chanceRealUmEm)),
                modifier = Modifier.weight(1f),
            )
        }
        SeletorDeFechamento(quantidadeAtual = uiState.quantidadeDeDezenas, isPro = uiState.isPro)
        if (uiState.podeVerDesdobramentos) {
            Text(
                text = stringResource(id = R.string.detalhe_ver_desdobramentos_cta),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(role = Role.Button, onClick = onVerDesdobramentosClick),
            )
        }
        HorizontalDivider(color = NocturneOutline)
        Text(text = stringResource(id = R.string.detalhe_origem_titulo), style = MaterialTheme.typography.titleMedium)
        // RF-11.10 — o ritual dos amuletos aparece como fonte própria, ao
        // lado das crenças, na mesma seção "De onde vieram as dezenas".
        uiState.origensDoRitual.forEach { revelacao -> CartaoDeOrigemDoRitual(revelacao) }
        uiState.origens.forEach { origem -> CartaoDeOrigem(origem) }
        HorizontalDivider(color = NocturneOutline)
        Text(
            text = stringResource(id = R.string.detalhe_estatisticas_soma, uiState.soma),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(id = R.string.detalhe_estatisticas_pares_impares, uiState.pares, uiState.impares),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(id = R.string.detalhe_estatisticas_moldura_miolo, uiState.moldura, uiState.miolo),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(id = R.string.detalhe_estatisticas_custo, formatarReais(uiState.custo)),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (uiState.dezenasFixas.isNotEmpty()) {
            FixasChip(dezenasFixas = uiState.dezenasFixas, onLimparClick = onLimparFixasClick)
        }
    }
}

@Composable
private fun CaixaDeDestaque(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
    ) {
        Text(text = titulo, style = MaterialTheme.typography.bodySmall)
        Text(text = valor, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun SeletorDeFechamento(
    quantidadeAtual: Int,
    isPro: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(id = R.string.detalhe_fechamento_titulo),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        listOf(15, 16, 18, 20).forEach { tamanho ->
            val ativo = tamanho == quantidadeAtual
            // RF-09 — 15 nunca precisa de Pro; os demais só destravam com isPro.
            val bloqueado = tamanho != 15 && !isPro
            val descricaoBloqueado = stringResource(id = R.string.detalhe_fechamento_bloqueado_descricao, tamanho)
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(
                            color = if (ativo) NocturneSurface else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                        ).border(
                            border = BorderStroke(1.dp, if (ativo) NocturneAccent else NocturneOutline),
                            shape = RoundedCornerShape(6.dp),
                        ).padding(vertical = 8.dp)
                        .then(
                            if (bloqueado) Modifier.semantics { contentDescription = descricaoBloqueado } else Modifier,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (bloqueado) "$tamanho 🔒" else tamanho.toString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun CartaoDeOrigem(
    origem: OrigemDeDezenasUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = nomeDe(origem.crenca)), style = MaterialTheme.typography.titleSmall)
            Text(text = stringResource(id = descricaoDe(origem.crenca)), style = MaterialTheme.typography.bodySmall)
            if (origem.dezenas.isNotEmpty()) {
                Text(
                    text = origem.dezenas.joinToString(" · ") { "%02d".format(it) },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun CartaoDeOrigemDoRitual(
    revelacao: RevelacaoDoAmuleto,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneAccent), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = emojiDoAmuleto(revelacao.amuleto)),
            style = MaterialTheme.typography.titleMedium,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = nomeDoAmuleto(revelacao.amuleto)),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(id = fraseDaEscolha(revelacao.opcao)),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(text = "%02d".format(revelacao.dezena), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FixasChip(
    dezenasFixas: List<Int>,
    onLimparClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                stringResource(
                    id = R.string.detalhe_fixas_rotulo,
                    dezenasFixas.joinToString(" · ") { "%02d".format(it) },
                ),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(id = R.string.detalhe_fixas_limpar),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.clickable(role = Role.Button, onClick = onLimparClick),
        )
    }
}

@Composable
private fun SecaoEdicao(
    uiState: DetalheUiState,
    onAlternarGuardarFixasClick: () -> Unit,
    onCancelarClick: () -> Unit,
    onSalvarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text =
                    stringResource(
                        id = R.string.detalhe_editar_contagem,
                        uiState.dezenasEmEdicao.size,
                        uiState.quantidadeDeDezenas,
                    ),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(text = textoDeContagem(uiState.faltamOuSobram), style = MaterialTheme.typography.bodySmall)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(role = Role.Checkbox, onClick = onAlternarGuardarFixasClick),
        ) {
            Checkbox(
                checked = uiState.guardarComoFixasAoSalvar,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = NocturneAccent),
            )
            Text(
                text = stringResource(id = R.string.detalhe_editar_guardar_fixas),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotaoPrimario(
                texto = stringResource(id = R.string.detalhe_editar_cancelar),
                onClick = onCancelarClick,
                modifier = Modifier.weight(1f),
            )
            BotaoPrimario(
                texto = stringResource(id = R.string.detalhe_editar_salvar),
                onClick = onSalvarClick,
                enabled = uiState.faltamOuSobram == 0,
                modifier = Modifier.weight(1f).testTag(tagBotaoSalvarEdicao()),
            )
        }
    }
}

@Composable
private fun textoDeContagem(faltamOuSobram: Int): String =
    when {
        faltamOuSobram > 0 ->
            pluralStringResource(id = R.plurals.detalhe_editar_marque_mais, count = faltamOuSobram, faltamOuSobram)
        faltamOuSobram < 0 ->
            pluralStringResource(id = R.plurals.detalhe_editar_desmarque, count = -faltamOuSobram, -faltamOuSobram)
        else -> stringResource(id = R.string.detalhe_editar_contagem_certa)
    }

private fun formatarReais(valor: java.math.BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)

private fun formatarInteiro(valor: Long): String = NumberFormat.getIntegerInstance(Locale("pt", "BR")).format(valor)

private fun nomeDe(crenca: Crenca): Int =
    when (crenca) {
        Crenca.SIGNO -> R.string.crenca_signo_nome
        Crenca.NASCIMENTO -> R.string.crenca_nascimento_nome
        Crenca.QUENTES -> R.string.crenca_quentes_nome
        Crenca.ATRASADOS -> R.string.crenca_atrasados_nome
        Crenca.LUA -> R.string.crenca_lua_nome
        Crenca.SONHO -> R.string.crenca_sonho_nome
        Crenca.MOLDURA -> R.string.crenca_moldura_nome
        Crenca.PARES -> R.string.crenca_pares_nome
        Crenca.PRIMOS -> R.string.crenca_primos_nome
        Crenca.SOMA -> R.string.crenca_soma_nome
        Crenca.REPETIDAS -> R.string.crenca_repetidas_nome
        Crenca.NUMEROLOGIA -> R.string.crenca_numerologia_nome
    }

private fun descricaoDe(crenca: Crenca): Int =
    when (crenca) {
        Crenca.SIGNO -> R.string.crenca_signo_desc
        Crenca.NASCIMENTO -> R.string.crenca_nascimento_desc
        Crenca.QUENTES -> R.string.crenca_quentes_desc
        Crenca.ATRASADOS -> R.string.crenca_atrasados_desc
        Crenca.LUA -> R.string.crenca_lua_desc
        Crenca.SONHO -> R.string.crenca_sonho_desc
        Crenca.MOLDURA -> R.string.crenca_moldura_desc
        Crenca.PARES -> R.string.crenca_pares_desc
        Crenca.PRIMOS -> R.string.crenca_primos_desc
        Crenca.SOMA -> R.string.crenca_soma_desc
        Crenca.REPETIDAS -> R.string.crenca_repetidas_desc
        Crenca.NUMEROLOGIA -> R.string.crenca_numerologia_desc
    }
