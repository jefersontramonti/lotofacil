package com.trevo.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import java.text.NumberFormat
import java.util.Locale

fun tagCartaoPalpite(id: Long): String = "palpite_$id"

fun tagBotaoExcluirPalpite(id: Long): String = "palpite_${id}_excluir"

@Composable
fun TelaHome(
    uiState: HomeUiState,
    onExcluirClick: (Long) -> Unit,
    onConfirmarExclusaoClick: () -> Unit,
    onCancelarExclusaoClick: () -> Unit,
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
            CabecalhoHome()
            Text(text = stringResource(id = R.string.home_horario_apostas), style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = NocturneOutline)
            SecaoPalpites(
                uiState = uiState,
                onExcluirClick = onExcluirClick,
            )
            Text(
                text = stringResource(id = R.string.home_disclaimer_aposta),
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider(color = NocturneOutline)
            BarraDeNavegacao()
        }
    }

    val idParaConfirmar = uiState.palpiteParaConfirmarExclusao
    if (idParaConfirmar != null) {
        DialogoConfirmarExclusao(
            onConfirmarClick = onConfirmarExclusaoClick,
            onCancelarClick = onCancelarExclusaoClick,
        )
    }
}

@Composable
private fun CabecalhoHome(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .border(
                        border = BorderStroke(width = 1.5.dp, color = NocturneAccent),
                        shape = RoundedCornerShape(8.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.trevo),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun SecaoPalpites(
    uiState: HomeUiState,
    onExcluirClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.home_secao_palpites_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (uiState.palpitesHoje.isNotEmpty()) {
                Text(text = resumoDeJogos(uiState), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (uiState.palpitesHoje.isEmpty()) {
            EstadoVazio()
        } else {
            uiState.palpitesHoje.forEach { palpite ->
                CartaoPalpite(palpite = palpite, onExcluirClick = { onExcluirClick(palpite.id) })
            }
        }
    }
}

@Composable
private fun resumoDeJogos(uiState: HomeUiState): String {
    val custoFormatado = formatarReais(uiState.custoTotal)
    return pluralStringResource(
        id = R.plurals.home_resumo_jogos,
        count = uiState.totalDeJogos,
        uiState.totalDeJogos,
        custoFormatado,
    )
}

private fun formatarReais(valor: java.math.BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)

@Composable
private fun EstadoVazio(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = stringResource(id = R.string.home_vazio_titulo), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(id = R.string.home_vazio_descricao), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CartaoPalpite(
    palpite: PalpiteItemUiState,
    onExcluirClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoExcluir = stringResource(id = R.string.home_excluir_descricao, palpite.numeroDoDia)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
                .testTag(tagCartaoPalpite(palpite.id)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.home_palpite_rotulo, palpite.numeroDoDia),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(text = palpite.horario, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "🗑",
                modifier =
                    Modifier
                        .clickable(role = Role.Button, onClick = onExcluirClick)
                        .semantics { contentDescription = descricaoExcluir }
                        .testTag(tagBotaoExcluirPalpite(palpite.id)),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            palpite.dezenas.forEach { dezena ->
                Box(
                    modifier =
                        Modifier
                            .size(28.dp)
                            .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "%02d".format(dezena), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(
            text = stringResource(id = R.string.home_palpite_forca, palpite.forca),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun BarraDeNavegacao(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = stringResource(id = R.string.home_nav_inicio), style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(id = R.string.home_nav_conferir), style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(id = R.string.home_nav_historico), style = MaterialTheme.typography.labelMedium)
        Text(text = stringResource(id = R.string.home_nav_perfil), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun DialogoConfirmarExclusao(
    onConfirmarClick: () -> Unit,
    onCancelarClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelarClick,
        title = { Text(text = stringResource(id = R.string.home_excluir_confirmar_titulo)) },
        text = { Text(text = stringResource(id = R.string.home_excluir_confirmar_mensagem)) },
        confirmButton = {
            TextButton(onClick = onConfirmarClick) {
                Text(text = stringResource(id = R.string.home_excluir_confirmar_cta))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelarClick) {
                Text(text = stringResource(id = R.string.home_excluir_cancelar_cta))
            }
        },
    )
}
