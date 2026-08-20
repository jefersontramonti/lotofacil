package com.trevo.app.assinatura

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.data.assinatura.PRODUTO_ID_ANUAL
import com.trevo.core.data.assinatura.ProdutoDeAssinatura
import com.trevo.core.ui.BotaoPrimario
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import com.trevo.core.ui.NocturneText

const val TAG_PAYWALL_FECHAR = "paywall_fechar"
const val TAG_PAYWALL_CTA_COMECAR = "paywall_cta_comecar"
const val TAG_PAYWALL_CTA_CONTINUAR_GRATIS = "paywall_cta_continuar_gratis"

fun tagPaywallPlano(productId: String): String = "paywall_plano_$productId"

private data class ItemPro(
    val icone: String,
    val nome: Int,
    val descricao: Int,
)

private val ITENS_PRO =
    listOf(
        ItemPro("∞", R.string.paywall_item_crencas_nome, R.string.paywall_item_crencas_desc),
        ItemPro("✦", R.string.paywall_item_palpites_nome, R.string.paywall_item_palpites_desc),
        ItemPro("▦", R.string.paywall_item_fechamento_nome, R.string.paywall_item_fechamento_desc),
        ItemPro("✓", R.string.paywall_item_conferencia_nome, R.string.paywall_item_conferencia_desc),
        ItemPro("↻", R.string.paywall_item_historico_nome, R.string.paywall_item_historico_desc),
        ItemPro("⤓", R.string.paywall_item_exportar_nome, R.string.paywall_item_exportar_desc),
    )

@Composable
fun TelaPaywall(
    uiState: PaywallUiState,
    onFecharClick: () -> Unit,
    onEscolherPlanoClick: (String) -> Unit,
    onComecarTesteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    ).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CabecalhoDoPaywall(onFecharClick)
            if (uiState.carregando) {
                Text(text = "", style = MaterialTheme.typography.bodySmall)
            } else if (uiState.indisponivel) {
                Text(
                    text = stringResource(id = R.string.paywall_indisponivel),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                ConteudoDoPaywall(uiState, onEscolherPlanoClick, onComecarTesteClick)
            }
            Text(
                text = stringResource(id = R.string.paywall_cta_continuar_gratis),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onFecharClick)
                        .testTag(TAG_PAYWALL_CTA_CONTINUAR_GRATIS)
                        .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun CabecalhoDoPaywall(
    onFecharClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoFechar = stringResource(id = R.string.paywall_fechar_descricao)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(32.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "✕",
            modifier =
                Modifier
                    .width(32.dp)
                    .clickable(role = Role.Button, onClick = onFecharClick)
                    .semantics { contentDescription = descricaoFechar }
                    .testTag(TAG_PAYWALL_FECHAR)
                    .padding(4.dp),
        )
    }
}

@Composable
private fun ConteudoDoPaywall(
    uiState: PaywallUiState,
    onEscolherPlanoClick: (String) -> Unit,
    onComecarTesteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SelinhoDoPaywall(stringResource(id = R.string.paywall_tag_pro))
            SelinhoDoPaywall(stringResource(id = R.string.paywall_tag_trial), destaque = true)
        }
        Text(text = stringResource(id = R.string.paywall_titulo), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(id = R.string.paywall_subtitulo), style = MaterialTheme.typography.bodyMedium)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ITENS_PRO.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Text(text = item.icone, color = NocturneAccent)
                    Column {
                        Text(text = stringResource(id = item.nome), style = MaterialTheme.typography.bodyMedium)
                        Text(text = stringResource(id = item.descricao), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
        ) {
            Text(text = stringResource(id = R.string.paywall_teste_titulo), style = MaterialTheme.typography.labelLarge)
            Text(
                text = stringResource(id = R.string.paywall_teste_hoje),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(text = stringResource(id = R.string.paywall_teste_dia5), style = MaterialTheme.typography.bodySmall)
            Text(text = stringResource(id = R.string.paywall_teste_dia7), style = MaterialTheme.typography.bodySmall)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.produtos.forEach { produto ->
                CartaoDePlano(
                    produto = produto,
                    selecionado = produto.productId == uiState.produtoSelecionadoId,
                    onClick = { onEscolherPlanoClick(produto.productId) },
                )
            }
        }

        BotaoPrimario(
            texto = stringResource(id = R.string.paywall_cta_comecar_teste),
            onClick = onComecarTesteClick,
            modifier = Modifier.fillMaxWidth().testTag(TAG_PAYWALL_CTA_COMECAR),
        )
        val produtoSelecionado = uiState.produtoSelecionado
        if (produtoSelecionado != null) {
            Text(
                text =
                    stringResource(
                        id = R.string.paywall_nota_pos_teste,
                        produtoSelecionado.precoFormatado,
                        stringResource(id = nomeDoPlano(produtoSelecionado.productId)),
                    ),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SelinhoDoPaywall(
    texto: String,
    destaque: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = if (destaque) NocturneText else NocturneAccent,
        modifier =
            modifier
                .background(
                    color = if (destaque) NocturneAccent else NocturneSurface,
                    shape = RoundedCornerShape(50),
                ).border(
                    border = BorderStroke(1.dp, NocturneAccent),
                    shape = RoundedCornerShape(50),
                ).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun CartaoDePlano(
    produto: ProdutoDeAssinatura,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detalhe =
        if (produto.productId == PRODUTO_ID_ANUAL) {
            R.string.paywall_plano_anual_detalhe
        } else {
            R.string.paywall_plano_mensal_detalhe
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = if (selecionado) NocturneSurface else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                ).border(
                    border =
                        BorderStroke(
                            if (selecionado) 2.dp else 1.dp,
                            if (selecionado) NocturneAccent else NocturneOutline,
                        ),
                    shape = RoundedCornerShape(8.dp),
                ).clickable(role = Role.RadioButton, onClick = onClick)
                .testTag(tagPaywallPlano(produto.productId))
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = nomeDoPlano(produto.productId)),
                    style = MaterialTheme.typography.titleSmall,
                )
                if (produto.productId == PRODUTO_ID_ANUAL) {
                    SelinhoDoPaywall(stringResource(id = R.string.paywall_plano_selo_mais_escolhido))
                }
            }
            Text(text = stringResource(id = detalhe), style = MaterialTheme.typography.bodySmall)
        }
        Text(text = produto.precoFormatado, style = MaterialTheme.typography.titleMedium)
    }
}
