package com.trevo.app.conferencia

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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trevo.app.R
import com.trevo.app.detalhe.GradeDeDezenas
import com.trevo.core.ui.BotaoPrimario
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

const val TAG_BOTAO_TENTAR_NOVAMENTE = "conferencia_tentar_novamente"
const val TAG_BOTAO_INFORMAR_MANUALMENTE = "conferencia_informar_manualmente"
const val TAG_BOTAO_CONFIRMAR_MANUAL = "conferencia_confirmar_manual"

@Composable
fun TelaConferencia(
    uiState: ConferenciaUiState,
    onVoltarClick: () -> Unit,
    onTentarNovamenteClick: () -> Unit,
    onInformarResultadoManualmente: (Set<Int>) -> Unit,
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
            Cabecalho(onVoltarClick = onVoltarClick)
            when (uiState) {
                is ConferenciaUiState.Carregando -> {
                    Text(
                        text = stringResource(id = R.string.conferencia_carregando),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is ConferenciaUiState.Espera -> {
                    EstadoDeAviso(emoji = "🕓", titulo = stringResource(id = R.string.conferencia_espera_titulo)) {
                        Text(
                            text = stringResource(id = R.string.conferencia_espera_descricao),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                is ConferenciaUiState.SemConexao -> {
                    EstadoDeFalha(
                        emoji = "📵",
                        titulo = stringResource(id = R.string.conferencia_sem_conexao_titulo),
                        descricao = stringResource(id = R.string.conferencia_sem_conexao_descricao),
                        onTentarNovamenteClick = onTentarNovamenteClick,
                        onInformarResultadoManualmente = onInformarResultadoManualmente,
                    )
                }
                is ConferenciaUiState.Falha -> {
                    EstadoDeFalha(
                        emoji = "⚠",
                        titulo = stringResource(id = R.string.conferencia_falha_titulo),
                        descricao = stringResource(id = R.string.conferencia_falha_descricao),
                        onTentarNovamenteClick = onTentarNovamenteClick,
                        onInformarResultadoManualmente = onInformarResultadoManualmente,
                    )
                }
                is ConferenciaUiState.Sucesso -> SecaoSucesso(uiState)
            }
        }
    }
}

@Composable
private fun Cabecalho(
    onVoltarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoVoltar = stringResource(id = R.string.conferencia_voltar_descricao)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "←",
            modifier =
                Modifier
                    .clickable(role = Role.Button, onClick = onVoltarClick)
                    .semantics { contentDescription = descricaoVoltar },
        )
        Text(
            text = stringResource(id = R.string.conferencia_titulo),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun EstadoDeAviso(
    emoji: String,
    titulo: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
        Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        conteudo()
    }
}

@Composable
private fun EstadoDeFalha(
    emoji: String,
    titulo: String,
    descricao: String,
    onTentarNovamenteClick: () -> Unit,
    onInformarResultadoManualmente: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarDialogoManual by remember { mutableStateOf(false) }

    EstadoDeAviso(emoji = emoji, titulo = titulo, modifier = modifier) {
        Text(
            text = descricao,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        BotaoPrimario(
            texto = stringResource(id = R.string.conferencia_tentar_novamente_cta),
            onClick = onTentarNovamenteClick,
            modifier = Modifier.padding(top = 4.dp).testTag(TAG_BOTAO_TENTAR_NOVAMENTE),
        )
        Text(
            text = stringResource(id = R.string.conferencia_informar_manualmente_cta),
            style = MaterialTheme.typography.bodySmall,
            modifier =
                Modifier
                    .padding(top = 4.dp)
                    .clickable(role = Role.Button) { mostrarDialogoManual = true }
                    .testTag(TAG_BOTAO_INFORMAR_MANUALMENTE),
        )
    }

    if (mostrarDialogoManual) {
        DialogoInformarResultadoManual(
            onCancelarClick = { mostrarDialogoManual = false },
            onConfirmarClick = { dezenas ->
                mostrarDialogoManual = false
                onInformarResultadoManualmente(dezenas)
            },
        )
    }
}

@Composable
private fun DialogoInformarResultadoManual(
    onCancelarClick: () -> Unit,
    onConfirmarClick: (Set<Int>) -> Unit,
) {
    var dezenasSelecionadas by remember { mutableStateOf(emptySet<Int>()) }

    Dialog(onDismissRequest = onCancelarClick) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, NocturneOutline),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(id = R.string.conferencia_informar_manualmente_titulo),
                    style = MaterialTheme.typography.titleMedium,
                )
                GradeDeDezenas(
                    dezenasMarcadas = dezenasSelecionadas,
                    dezenasFixas = emptySet(),
                    onDezenaClick = { dezena ->
                        dezenasSelecionadas =
                            if (dezena in dezenasSelecionadas) {
                                dezenasSelecionadas - dezena
                            } else {
                                dezenasSelecionadas + dezena
                            }
                    },
                )
                Text(
                    text =
                        stringResource(
                            id = R.string.conferencia_informar_manualmente_contagem,
                            dezenasSelecionadas.size,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(id = R.string.conferencia_informar_manualmente_cancelar_cta),
                        modifier = Modifier.clickable(role = Role.Button, onClick = onCancelarClick),
                    )
                    if (dezenasSelecionadas.size == 15) {
                        Text(
                            text = stringResource(id = R.string.conferencia_informar_manualmente_confirmar_cta),
                            color = NocturneAccent,
                            modifier =
                                Modifier
                                    .clickable(role = Role.Button) { onConfirmarClick(dezenasSelecionadas) }
                                    .testTag(TAG_BOTAO_CONFIRMAR_MANUAL),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecaoSucesso(
    uiState: ConferenciaUiState.Sucesso,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
        ) {
            val tituloConcurso =
                if (uiState.origemManual || uiState.numeroDoConcurso == null) {
                    stringResource(id = R.string.conferencia_resultado_manual_titulo)
                } else {
                    stringResource(id = R.string.conferencia_concurso_titulo, uiState.numeroDoConcurso)
                }
            Text(text = tituloConcurso, style = MaterialTheme.typography.bodySmall)
            LinhaDeDezenas(
                dezenas = uiState.dezenasSorteadas,
                dezenasCheias = uiState.dezenasSorteadas.toSet(),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        if (uiState.itens.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CartaoDeTotal(
                    rotulo = stringResource(id = R.string.conferencia_ganho_titulo),
                    valor = formatarReais(uiState.totalGanho),
                    modifier = Modifier.weight(1f),
                )
                CartaoDeTotal(
                    rotulo = stringResource(id = R.string.conferencia_gasto_titulo),
                    valor = formatarReais(uiState.totalGasto),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (uiState.itens.isEmpty()) {
            EstadoDeAviso(emoji = "🍀", titulo = stringResource(id = R.string.conferencia_vazio_titulo)) {
                Text(
                    text = stringResource(id = R.string.conferencia_vazio_descricao),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            uiState.itens.forEach { item -> CartaoDePalpiteConferido(item, origemManual = uiState.origemManual) }
        }

        Text(
            text = stringResource(id = R.string.conferencia_disclaimer_oficial),
            style = MaterialTheme.typography.bodySmall,
        )
        if (uiState.itens.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.conferencia_legenda_bolas),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CartaoDeTotal(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
    ) {
        Text(text = rotulo, style = MaterialTheme.typography.bodySmall)
        Text(text = valor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CartaoDePalpiteConferido(
    item: PalpiteConferidoUiState,
    origemManual: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.home_palpite_rotulo, item.numeroDoDia),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(text = rotuloDeAcertos(item, origemManual), style = MaterialTheme.typography.bodySmall)
        }
        LinhaDeDezenas(dezenas = item.dezenas, dezenasCheias = item.dezenasAcertadas)
        when {
            item.premio != null ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.conferencia_premio_rotulo),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = formatarReais(item.premio), style = MaterialTheme.typography.bodySmall)
                }
            origemManual ->
                Text(
                    text = stringResource(id = R.string.conferencia_premio_indisponivel),
                    style = MaterialTheme.typography.bodySmall,
                )
            else -> Unit
        }
    }
}

@Composable
private fun rotuloDeAcertos(
    item: PalpiteConferidoUiState,
    origemManual: Boolean,
): String {
    val acertos = pluralStringResource(id = R.plurals.conferencia_acertos, count = item.acertos, item.acertos)
    return if (item.premio == null && !origemManual) {
        "$acertos · ${stringResource(id = R.string.conferencia_sem_premio)}"
    } else {
        acertos
    }
}

@Composable
private fun LinhaDeDezenas(
    dezenas: List<Int>,
    dezenasCheias: Set<Int>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        dezenas.forEach { dezena ->
            val cheia = dezena in dezenasCheias
            val idDaDescricao =
                if (cheia) R.string.detalhe_dezena_marcada_descricao else R.string.detalhe_dezena_nao_marcada_descricao
            val descricao = stringResource(id = idDaDescricao, dezena)
            Box(
                modifier =
                    Modifier
                        .size(28.dp)
                        .background(if (cheia) NocturneAccent else Color.Transparent, RoundedCornerShape(50))
                        .border(BorderStroke(1.dp, NocturneOutline), RoundedCornerShape(50))
                        .semantics { contentDescription = descricao },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "%02d".format(dezena), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun formatarReais(valor: BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
