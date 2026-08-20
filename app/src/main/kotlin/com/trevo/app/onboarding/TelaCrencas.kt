package com.trevo.app.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.ui.BotaoPrimario
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface

// Prefixo público — TelaCrencasTest referencia estas funções para montar
// as mesmas tags sem duplicar a string literal.
fun tagCartaoCrenca(crenca: Crenca): String = "crenca_${crenca.name.lowercase()}"

fun tagCadeadoCrenca(crenca: Crenca): String = "${tagCartaoCrenca(crenca)}_cadeado"

@Composable
fun TelaCrencas(
    uiState: CrencasUiState,
    onCrencaClick: (Crenca) -> Unit,
    onCrencaBloqueadaClick: () -> Unit,
    onVoltarClick: () -> Unit,
    onContinuarClick: () -> Unit,
    // RF-07.2 reaproveita esta tela como "Suas crenças" a partir do Perfil —
    // lá o CTA final é "Salvar", não "Entrar no app" do onboarding.
    textoContinuar: String = stringResource(id = R.string.crencas_cta_entrar_no_app),
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.crencas_titulo),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(text = subtituloComContagem(uiState.selecionadas.size), style = MaterialTheme.typography.bodyLarge)
            Crenca.entries.forEach { crenca ->
                CartaoCrenca(
                    crenca = crenca,
                    selecionada = crenca in uiState.selecionadas,
                    bloqueada = uiState.crencaBloqueada(crenca),
                    onClick = { onCrencaClick(crenca) },
                    onBloqueadaClick = onCrencaBloqueadaClick,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BotaoPrimario(
                    texto = stringResource(id = R.string.crencas_cta_voltar),
                    onClick = onVoltarClick,
                    modifier = Modifier.weight(1f),
                )
                BotaoPrimario(
                    texto = textoContinuar,
                    onClick = onContinuarClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun subtituloComContagem(quantidadeSelecionada: Int) =
    buildAnnotatedString {
        append(stringResource(id = R.string.crencas_subtitulo_prefixo))
        append(" ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(
                pluralStringResource(
                    id = R.plurals.crencas_subtitulo_selecionadas,
                    count = quantidadeSelecionada,
                    quantidadeSelecionada,
                ),
            )
        }
    }

@Composable
private fun CartaoCrenca(
    crenca: Crenca,
    selecionada: Boolean,
    bloqueada: Boolean,
    onClick: () -> Unit,
    onBloqueadaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(if (bloqueada) 0.5f else 1f)
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .then(
                    if (bloqueada) {
                        Modifier.clickable(role = Role.Button, onClick = onBloqueadaClick)
                    } else {
                        Modifier.toggleable(value = selecionada, onValueChange = { onClick() }, role = Role.Checkbox)
                    },
                ).semantics(mergeDescendants = true) {}
                .padding(12.dp)
                .testTag(tagCartaoCrenca(crenca)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(id = iconeDe(crenca)),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = nomeDe(crenca)), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(id = descricaoDe(crenca)), style = MaterialTheme.typography.bodySmall)
        }
        if (bloqueada) {
            // RF-01.8: cadeado no lugar da caixa de marcação — mesmo
            // padrão de glifo-como-ícone já usado em
            // identidade_erro_indicador ("⚠"), sem depender de uma nova
            // biblioteca de ícones. TestTag próprio: a Row usa
            // mergeDescendants, então esse nó só é alcançável em teste com
            // useUnmergedTree = true.
            Text(
                text = stringResource(id = R.string.crencas_cadeado_indicador),
                modifier = Modifier.testTag(tagCadeadoCrenca(crenca)),
            )
        } else {
            // `onCheckedChange = null`: o toque é tratado pelo `toggleable` da Row;
            // o Checkbox aqui é só o indicador visual, mesclado no nó semântico
            // acima — RNF-03.5, o estado de marcação nunca é só a cor de fundo.
            Checkbox(
                checked = selecionada,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = NocturneAccent),
            )
        }
    }
}

@StringRes
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

@StringRes
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

@DrawableRes
private fun iconeDe(crenca: Crenca): Int =
    when (crenca) {
        Crenca.SIGNO -> R.drawable.ic_crenca_signo
        Crenca.NASCIMENTO -> R.drawable.ic_crenca_nascimento
        Crenca.QUENTES -> R.drawable.ic_crenca_quentes
        Crenca.ATRASADOS -> R.drawable.ic_crenca_atrasados
        Crenca.LUA -> R.drawable.ic_crenca_lua
        Crenca.SONHO -> R.drawable.ic_crenca_sonho
        Crenca.MOLDURA -> R.drawable.ic_crenca_moldura
        Crenca.PARES -> R.drawable.ic_crenca_pares
        Crenca.PRIMOS -> R.drawable.ic_crenca_primos
        Crenca.SOMA -> R.drawable.ic_crenca_soma
        Crenca.REPETIDAS -> R.drawable.ic_crenca_repetidas
        Crenca.NUMEROLOGIA -> R.drawable.ic_crenca_nome
    }
