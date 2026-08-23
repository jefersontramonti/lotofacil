package com.trevo.app.ritual

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.engine.crenca.Amuleto
import com.trevo.core.engine.crenca.ORDEM_DO_RITUAL
import com.trevo.core.engine.crenca.OpcaoDeAmuleto
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import com.trevo.core.engine.crenca.opcoesDoAmuleto
import com.trevo.core.engine.palpite.TamanhoDeFechamento
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import com.trevo.core.ui.NocturneText
import kotlinx.coroutines.delay

private const val INTERVALO_DE_REVELACAO_MS = 1_800L
private const val INTERVALO_DE_REVELACAO_REDUZIDO_MS = 150L

fun tagOpcaoDoAmuleto(opcao: OpcaoDeAmuleto): String = "ritual_opcao_${opcao.name.lowercase()}"

fun tagTamanho(tamanho: TamanhoDeFechamento?): String = "ritual_tamanho_${tamanho?.quantidade ?: 15}"

const val TAG_RITUAL_FECHAR = "ritual_fechar"
const val TAG_RITUAL_REFAZER = "ritual_refazer"
const val TAG_RITUAL_MONTAR = "ritual_montar"

@Composable
fun TelaRitual(
    uiState: RitualUiState,
    onFecharClick: () -> Unit,
    onEscolherOpcao: (OpcaoDeAmuleto) -> Unit,
    onRevelacaoTerminou: () -> Unit,
    onRefazerClick: () -> Unit,
    onEscolherTamanhoClick: (TamanhoDeFechamento?) -> Unit,
    onTamanhoBloqueadoClick: () -> Unit,
    onMontarPalpiteClick: () -> Unit,
    modifier: Modifier = Modifier,
    movimentoReduzido: Boolean = false,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (uiState) {
            is RitualUiState.Carregando -> Unit
            is RitualUiState.Escolha ->
                TelaEscolhaDoAmuleto(
                    uiState = uiState,
                    onFecharClick = onFecharClick,
                    onEscolherOpcao = onEscolherOpcao,
                )
            is RitualUiState.Revelando ->
                TelaRevelacao(
                    uiState = uiState,
                    movimentoReduzido = movimentoReduzido,
                    onRevelacaoTerminou = onRevelacaoTerminou,
                )
            is RitualUiState.Resumo ->
                TelaResumoDoRitual(
                    uiState = uiState,
                    onRefazerClick = onRefazerClick,
                    onEscolherTamanhoClick = onEscolherTamanhoClick,
                    onTamanhoBloqueadoClick = onTamanhoBloqueadoClick,
                    onMontarPalpiteClick = onMontarPalpiteClick,
                )
        }
    }
}

@Composable
private fun CabecalhoDoRitual(
    reveladas: List<RevelacaoDoAmuleto>,
    onFecharClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoFechar = stringResource(id = R.string.ritual_fechar_descricao)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clickable(role = Role.Button, onClick = onFecharClick)
                        .semantics { contentDescription = descricaoFechar }
                        .testTag(TAG_RITUAL_FECHAR),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✕")
            }
            Text(
                text = stringResource(id = R.string.ritual_titulo),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        LinhaDeChipsDoRitual(reveladas = reveladas)
    }
}

@Composable
private fun LinhaDeChipsDoRitual(
    reveladas: List<RevelacaoDoAmuleto>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        ORDEM_DO_RITUAL.forEach { amuleto ->
            val feito = reveladas.any { it.amuleto == amuleto }
            ChipDeAmuleto(amuleto = amuleto, feito = feito)
        }
    }
}

@Composable
private fun ChipDeAmuleto(
    amuleto: Amuleto,
    feito: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(id = emojiDoAmuleto(amuleto)),
        modifier =
            modifier
                .border(
                    border = BorderStroke(1.dp, if (feito) NocturneAccent else NocturneOutline),
                    shape = RoundedCornerShape(6.dp),
                ).clearAndSetSemantics {}
                .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@Composable
private fun TelaEscolhaDoAmuleto(
    uiState: RitualUiState.Escolha,
    onFecharClick: () -> Unit,
    onEscolherOpcao: (OpcaoDeAmuleto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CabecalhoDoRitual(reveladas = uiState.reveladas, onFecharClick = onFecharClick)
        Text(
            text =
                stringResource(
                    id = R.string.ritual_amuleto_progresso,
                    uiState.indice,
                    uiState.total,
                    stringResource(id = conceitoDoAmuleto(uiState.amuletoAtual)),
                ),
            style = MaterialTheme.typography.labelLarge,
            color = NocturneAccent,
        )
        Text(
            text = stringResource(id = nomeDoAmuleto(uiState.amuletoAtual)),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(id = perguntaDoAmuleto(uiState.amuletoAtual)),
            style = MaterialTheme.typography.bodyLarge,
        )

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .border(border = BorderStroke(1.5.dp, NocturneOutline), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = emojiDoAmuleto(uiState.amuletoAtual)),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        val opcoes = opcoesDoAmuleto(uiState.amuletoAtual)
        val porLinha = if (opcoes.size == 4) 2 else opcoes.size
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            opcoes.chunked(porLinha).forEach { linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    linha.forEach { opcao ->
                        CartaoDeOpcao(
                            opcao = opcao,
                            onClick = { onEscolherOpcao(opcao) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (uiState.reveladas.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.ritual_ja_revelado),
                    style = MaterialTheme.typography.bodySmall,
                )
                uiState.reveladas.forEach { revelacao ->
                    Text(
                        text =
                            "${stringResource(
                                id = emojiDoAmuleto(revelacao.amuleto),
                            )} %02d".format(revelacao.dezena),
                        style = MaterialTheme.typography.labelSmall,
                        modifier =
                            Modifier
                                .background(color = NocturneAccent, shape = RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.background,
                    )
                }
            }
        }

        Text(
            text = stringResource(id = R.string.ritual_disclaimer_escolha),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun CartaoDeOpcao(
    opcao: OpcaoDeAmuleto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) {}
                .testTag(tagOpcaoDoAmuleto(opcao))
                .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stringResource(id = emojiDoAmuleto(opcao.amuleto)))
        Text(
            text = stringResource(id = rotuloDaOpcao(opcao)),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
        // RF-11.5 — indicador neutro "?", nunca a dezena, nunca uma pista dela.
        Text(text = stringResource(id = R.string.ritual_opcao_indicador), color = NocturneText.copy(alpha = 0.4f))
    }
}

@Composable
private fun TelaRevelacao(
    uiState: RitualUiState.Revelando,
    movimentoReduzido: Boolean,
    onRevelacaoTerminou: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.ultimaRevelacao) {
        delay(if (movimentoReduzido) INTERVALO_DE_REVELACAO_REDUZIDO_MS else INTERVALO_DE_REVELACAO_MS)
        onRevelacaoTerminou()
    }
    Box(modifier = modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CirculoDeRevelacao(dezena = uiState.ultimaRevelacao.dezena, movimentoReduzido = movimentoReduzido)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = nomeDoAmuleto(uiState.ultimaRevelacao.amuleto)),
                    style = MaterialTheme.typography.labelLarge,
                    color = NocturneAccent,
                )
                Text(
                    text = stringResource(id = conceitoDoAmuleto(uiState.ultimaRevelacao.amuleto)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(id = fraseDaEscolha(uiState.ultimaRevelacao.opcao)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun CirculoDeRevelacao(
    dezena: Int,
    movimentoReduzido: Boolean,
    modifier: Modifier = Modifier,
) {
    // RF-11.6 — giro e halo; respeita a preferência de movimento reduzido do
    // sistema (CLAUDE.md §6), igual ao ícone de TelaGerando.
    val halo =
        if (movimentoReduzido) {
            1f
        } else {
            val transicaoInfinita = rememberInfiniteTransition(label = "haloDaRevelacao")
            val haloAnimado by
                transicaoInfinita.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900, easing = LinearEasing)),
                    label = "escalaDoHalo",
                )
            haloAnimado
        }
    Box(modifier = modifier.size(110.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = halo
                        scaleY = halo
                        alpha = 0.5f
                    }.border(border = BorderStroke(1.5.dp, NocturneAccent), shape = CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .size(88.dp)
                    .background(color = NocturneSurface, shape = CircleShape)
                    .border(border = BorderStroke(2.dp, NocturneAccent), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "%02d".format(dezena),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TelaResumoDoRitual(
    uiState: RitualUiState.Resumo,
    onRefazerClick: () -> Unit,
    onEscolherTamanhoClick: (TamanhoDeFechamento?) -> Unit,
    onTamanhoBloqueadoClick: () -> Unit,
    onMontarPalpiteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(id = R.string.ritual_titulo),
                style = MaterialTheme.typography.titleMedium,
            )
            LinhaDeChipsDoRitual(reveladas = uiState.reveladas)
        }
        Text(
            text = stringResource(id = R.string.ritual_resumo_kicker),
            style = MaterialTheme.typography.labelLarge,
            color = NocturneAccent,
        )
        Text(text = stringResource(id = R.string.ritual_resumo_titulo), style = MaterialTheme.typography.headlineSmall)

        uiState.reveladas.forEach { revelacao -> CartaoDeRevelacaoNoResumo(revelacao) }

        Text(
            text =
                pluralStringResource(
                    id = R.plurals.ritual_resumo_nota,
                    count = uiState.quantidadeDeOutrasDezenas,
                    uiState.quantidadeDeOutrasDezenas,
                ),
            style = MaterialTheme.typography.bodySmall,
        )

        // RF-02.8 — fechamento também no ritual: os amuletos continuam
        // forçando as mesmas dezenas, só muda quantas vêm de crenças/
        // estatística. 16/18/20 ficam bloqueados até existir Pro de verdade
        // (RF-09) — mesmo padrão visível-mas-bloqueado do SeletorDeFechamento
        // de TelaDetalhe, só que aqui é uma escolha de verdade, não uma
        // exibição do que o palpite já é.
        SeletorDeFechamentoDoRitual(
            tamanhoSelecionado = uiState.tamanho,
            isPro = uiState.isPro,
            onEscolherClick = onEscolherTamanhoClick,
            onBloqueadoClick = onTamanhoBloqueadoClick,
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val descricaoRefazer = stringResource(id = R.string.ritual_resumo_refazer_descricao)
            Text(
                text = "↻",
                modifier =
                    Modifier
                        .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onRefazerClick)
                        .semantics { contentDescription = descricaoRefazer }
                        .testTag(TAG_RITUAL_REFAZER)
                        .padding(14.dp),
            )
            Text(
                text = stringResource(id = R.string.ritual_resumo_montar_cta),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .weight(1f)
                        .border(border = BorderStroke(1.dp, NocturneAccent), shape = RoundedCornerShape(8.dp))
                        .clickable(
                            role = Role.Button,
                            enabled = !uiState.montandoPalpite,
                            onClick = onMontarPalpiteClick,
                        ).testTag(TAG_RITUAL_MONTAR)
                        .padding(14.dp),
            )
        }
    }
}

@Composable
private fun SeletorDeFechamentoDoRitual(
    tamanhoSelecionado: TamanhoDeFechamento?,
    isPro: Boolean,
    onEscolherClick: (TamanhoDeFechamento?) -> Unit,
    onBloqueadoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(id = R.string.ritual_fechamento_titulo),
            style = MaterialTheme.typography.labelMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ChipDeTamanho(
                tamanho = null,
                quantidade = 15,
                selecionado = tamanhoSelecionado == null,
                bloqueado = false,
                onClick = { onEscolherClick(null) },
                modifier = Modifier.weight(1f),
            )
            TamanhoDeFechamento.entries.forEach { tamanho ->
                val bloqueado = !isPro
                ChipDeTamanho(
                    tamanho = tamanho,
                    quantidade = tamanho.quantidade,
                    selecionado = tamanhoSelecionado == tamanho,
                    bloqueado = bloqueado,
                    onClick = { if (bloqueado) onBloqueadoClick() else onEscolherClick(tamanho) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ChipDeTamanho(
    tamanho: TamanhoDeFechamento?,
    quantidade: Int,
    selecionado: Boolean,
    bloqueado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoBloqueado = stringResource(id = R.string.ritual_fechamento_bloqueado_descricao, quantidade)
    Column(
        modifier =
            modifier
                .background(
                    color = if (selecionado) NocturneSurface else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                ).border(
                    border =
                        BorderStroke(
                            if (selecionado) 2.dp else 1.dp,
                            if (selecionado) NocturneAccent else NocturneOutline,
                        ),
                    shape = RoundedCornerShape(6.dp),
                ).selectable(selected = selecionado, onClick = onClick, role = Role.RadioButton)
                .then(if (bloqueado) Modifier.semantics { contentDescription = descricaoBloqueado } else Modifier)
                .testTag(tagTamanho(tamanho))
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (bloqueado) "$quantidade 🔒" else quantidade.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun CartaoDeRevelacaoNoResumo(
    revelacao: RevelacaoDoAmuleto,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .semantics(mergeDescendants = true) {}
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = emojiDoAmuleto(revelacao.amuleto)),
            style = MaterialTheme.typography.headlineSmall,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = nomeDoAmuleto(revelacao.amuleto)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = fraseDaEscolha(revelacao.opcao)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Box(
            modifier =
                Modifier
                    .size(30.dp)
                    .background(color = NocturneAccent, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "%02d".format(revelacao.dezena),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.background,
            )
        }
    }
}
