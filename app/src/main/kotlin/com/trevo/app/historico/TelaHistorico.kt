package com.trevo.app.historico

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

const val TAG_BOTAO_VER_MAIS_HISTORICO = "historico_ver_mais"
const val TAG_BOTAO_ASSINAR_HISTORICO = "historico_assinar"

@Composable
fun TelaHistorico(
    uiState: HistoricoUiState,
    onVerMaisClick: () -> Unit,
    onAssinarClick: () -> Unit = {},
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
            Text(text = stringResource(id = R.string.historico_titulo), style = MaterialTheme.typography.headlineSmall)
            when (uiState) {
                is HistoricoUiState.Carregando -> Unit
                is HistoricoUiState.Vazio -> EstadoVazio()
                is HistoricoUiState.ComDados -> ConteudoComDados(uiState, onVerMaisClick, onAssinarClick)
            }
        }
    }
}

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
        Text(text = stringResource(id = R.string.historico_vazio_titulo), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(id = R.string.historico_vazio_descricao),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConteudoComDados(
    uiState: HistoricoUiState.ComDados,
    onVerMaisClick: () -> Unit,
    onAssinarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text =
                pluralStringResource(
                    id = R.plurals.historico_resumo,
                    count = uiState.totalDeJogos,
                    uiState.totalDeJogos,
                    uiState.totalDeConcursos,
                ),
            style = MaterialTheme.typography.bodySmall,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CartaoDeTotal(
                rotulo = stringResource(id = R.string.historico_gastou_titulo),
                valor = formatarReais(uiState.totalGasto),
                modifier = Modifier.weight(1f),
            )
            CartaoDeTotal(
                rotulo = stringResource(id = R.string.historico_ganhou_titulo),
                valor = formatarReais(uiState.totalGanho),
                modifier = Modifier.weight(1f),
            )
        }

        CartaoDeSaldo(uiState)
        CartaoDeFaixas(uiState)

        uiState.concursosRevelados.forEach { concurso -> CartaoDeConcurso(concurso) }

        if (uiState.temMaisConcursos) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onVerMaisClick)
                        .testTag(TAG_BOTAO_VER_MAIS_HISTORICO),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        pluralStringResource(
                            id = R.plurals.historico_ver_mais_cta,
                            count = uiState.quantidadeDeConcursosRestantes,
                            uiState.quantidadeDeConcursosRestantes,
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (uiState.maisConcursosSoNoPro) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(border = BorderStroke(1.dp, NocturneAccent), shape = RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onAssinarClick)
                        .testTag(TAG_BOTAO_ASSINAR_HISTORICO),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.historico_mais_concursos_pro_cta),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
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
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
    ) {
        Text(text = rotulo, style = MaterialTheme.typography.bodySmall)
        Text(text = valor, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun CartaoDeSaldo(
    uiState: HistoricoUiState.ComDados,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.historico_saldo_titulo),
                style = MaterialTheme.typography.bodySmall,
            )
            // RF-10.4 — saldo negativo mostrado sem eufemismo, sinal explícito.
            Text(text = formatarSaldo(uiState.saldo), style = MaterialTheme.typography.titleLarge)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(id = R.string.historico_retorno, uiState.retornoPercentual),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(id = R.string.historico_media, formatarReais(uiState.mediaGastoPorConcurso)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CartaoDeFaixas(
    uiState: HistoricoUiState.ComDados,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.historico_faixas_titulo),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    stringResource(
                        id = R.string.historico_faixas_melhor,
                        uiState.melhorResultadoEmAcertos,
                    ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }
        val maiorContagem = uiState.faixas.maxOf { it.quantidade }.coerceAtLeast(1)
        uiState.faixas.forEach { faixa -> LinhaDeFaixa(faixa, maiorContagem) }
    }
}

@Composable
private fun LinhaDeFaixa(
    faixa: FaixaHistoricoUiState,
    maiorContagem: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(id = R.string.historico_faixa_rotulo, faixa.acertos),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(end = 8.dp),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(color = NocturneOutline, shape = RoundedCornerShape(3.dp)),
        ) {
            if (faixa.quantidade > 0) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(faixa.quantidade.toFloat() / maiorContagem)
                            .background(color = NocturneAccent, shape = RoundedCornerShape(3.dp)),
                )
            }
        }
        Text(
            text = faixa.quantidade.toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun CartaoDeConcurso(
    concurso: ConcursoConferidoUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val tituloConcurso =
                if (concurso.numero != null) {
                    stringResource(
                        id = R.string.historico_concurso_titulo,
                        concurso.numero,
                        formatarData(concurso.data),
                    )
                } else {
                    stringResource(id = R.string.historico_concurso_manual_titulo, formatarData(concurso.data))
                }
            Text(
                text = tituloConcurso,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            Text(text = formatarReais(concurso.premioTotal), style = MaterialTheme.typography.bodySmall)
        }
        concurso.palpites.forEach { palpite -> LinhaDePalpite(palpite) }
    }
}

@Composable
private fun LinhaDePalpite(
    palpite: PalpiteNoHistoricoUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.home_palpite_rotulo, palpite.numeroDoDia),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(text = rotuloDeAcertos(palpite), style = MaterialTheme.typography.bodySmall)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            palpite.dezenas.forEach { dezena ->
                Box(
                    modifier =
                        Modifier
                            .size(26.dp)
                            .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "%02d".format(dezena), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun rotuloDeAcertos(palpite: PalpiteNoHistoricoUiState): String {
    val acertos = pluralStringResource(id = R.plurals.conferencia_acertos, count = palpite.acertos, palpite.acertos)
    return if (palpite.premio != null) {
        "$acertos · ${formatarReais(palpite.premio)}"
    } else {
        "$acertos · ${stringResource(id = R.string.conferencia_sem_premio)}"
    }
}

private fun formatarReais(valor: BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)

private fun formatarSaldo(saldo: BigDecimal): String {
    val sinal = if (saldo.signum() < 0) "−" else "+"
    return "$sinal${formatarReais(saldo.abs())}"
}

private fun formatarData(data: LocalDate): String =
    data.format(DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR")))
