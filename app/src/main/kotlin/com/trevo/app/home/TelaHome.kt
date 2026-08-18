package com.trevo.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trevo.app.R
import com.trevo.core.engine.crenca.FaseDaLua
import com.trevo.core.engine.crenca.GRUPOS_DO_BICHO
import com.trevo.core.engine.crenca.GrupoDoBicho
import com.trevo.core.engine.crenca.dezenasDoGrupoDoBicho
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import java.text.NumberFormat
import java.util.Locale

fun tagCartaoPalpite(id: Long): String = "palpite_$id"

fun tagBotaoExcluirPalpite(id: Long): String = "palpite_${id}_excluir"

fun tagGrupoDoBicho(numero: Int): String = "grupo_do_bicho_$numero"

const val TAG_BOTAO_VER_GRUPOS = "botao_ver_todos_os_grupos"

@Composable
fun TelaHome(
    uiState: HomeUiState,
    onExcluirClick: (Long) -> Unit,
    onConfirmarExclusaoClick: () -> Unit,
    onCancelarExclusaoClick: () -> Unit,
    onPalpiteClick: (Long) -> Unit = {},
    onAlternarListaDeGruposClick: () -> Unit = {},
    onGrupoClick: (Int) -> Unit = {},
    onFecharDialogoSonhoClick: () -> Unit = {},
    onConfirmarSonhoClick: (Int) -> Unit = {},
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
            CabecalhoHome()
            Text(text = stringResource(id = R.string.home_horario_apostas), style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = NocturneOutline)
            if (uiState.nome != null) {
                SecaoSorteLuaSigno(uiState)
            }
            if (uiState.crencaSonhoAtiva) {
                SecaoSonho(
                    uiState = uiState,
                    onAlternarListaDeGruposClick = onAlternarListaDeGruposClick,
                    onGrupoClick = onGrupoClick,
                )
            }
            SecaoPalpites(
                uiState = uiState,
                onExcluirClick = onExcluirClick,
                onPalpiteClick = onPalpiteClick,
            )
            Text(
                text = stringResource(id = R.string.home_disclaimer_aposta),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (uiState.palpiteParaConfirmarExclusao != null) {
        DialogoConfirmarExclusao(
            onConfirmarClick = onConfirmarExclusaoClick,
            onCancelarClick = onCancelarExclusaoClick,
        )
    }

    val grupoAberto = uiState.grupoAbertoNoDialog
    if (grupoAberto != null) {
        DialogoCartaoDoSonho(
            grupo = grupoAberto,
            jaConfirmadoHoje = uiState.grupoDoSonhoConfirmadoHoje == grupoAberto.numero,
            onConfirmarClick = { onConfirmarSonhoClick(grupoAberto.numero) },
            onFecharClick = onFecharDialogoSonhoClick,
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
private fun SecaoSorteLuaSigno(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.home_saudacao, primeiroNomeDe(uiState.nome.orEmpty())),
            style = MaterialTheme.typography.titleLarge,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (uiState.indiceDeSorte != null) {
                CirculoDeSorte(indice = uiState.indiceDeSorte)
            }
            Column {
                Text(text = stringResource(id = R.string.home_sorte_titulo), style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.faseDaLua?.let { Tag(texto = stringResource(id = nomeDaFase(it))) }
                    uiState.signo?.let { Tag(texto = stringResource(id = nomeDoSigno(it))) }
                    uiState.diaDaSemanaAbreviado?.let { Tag(texto = it) }
                }
            }
        }
    }
}

@Composable
private fun CirculoDeSorte(
    indice: Int,
    modifier: Modifier = Modifier,
) {
    val corDeFundo = NocturneOutline
    val corDeProgresso = NocturneAccent
    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val larguraDoTraco = 4.dp.toPx()
            drawArc(
                color = corDeFundo,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = larguraDoTraco),
                size = Size(size.width - larguraDoTraco, size.height - larguraDoTraco),
                topLeft = Offset(larguraDoTraco / 2, larguraDoTraco / 2),
            )
            drawArc(
                color = corDeProgresso,
                startAngle = -90f,
                sweepAngle = 360f * (indice / 100f),
                useCenter = false,
                style = Stroke(width = larguraDoTraco),
                size = Size(size.width - larguraDoTraco, size.height - larguraDoTraco),
                topLeft = Offset(larguraDoTraco / 2, larguraDoTraco / 2),
            )
        }
        Text(text = indice.toString(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Tag(
    texto: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        modifier =
            modifier
                .background(color = NocturneSurface, shape = RoundedCornerShape(50))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

private fun primeiroNomeDe(nomeCompleto: String): String = nomeCompleto.trim().substringBefore(" ")

private fun nomeDaFase(fase: FaseDaLua): Int =
    when (fase) {
        FaseDaLua.NOVA -> R.string.fase_lua_nova
        FaseDaLua.CRESCENTE_INICIAL -> R.string.fase_lua_crescente_inicial
        FaseDaLua.QUARTO_CRESCENTE -> R.string.fase_lua_quarto_crescente
        FaseDaLua.CRESCENTE_GIBOSA -> R.string.fase_lua_crescente_gibosa
        FaseDaLua.CHEIA -> R.string.fase_lua_cheia
        FaseDaLua.MINGUANTE_GIBOSA -> R.string.fase_lua_minguante_gibosa
        FaseDaLua.QUARTO_MINGUANTE -> R.string.fase_lua_quarto_minguante
        FaseDaLua.MINGUANTE_FINAL -> R.string.fase_lua_minguante_final
    }

private fun nomeDoSigno(signo: Signo): Int =
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

@Composable
private fun SecaoSonho(
    uiState: HomeUiState,
    onAlternarListaDeGruposClick: () -> Unit,
    onGrupoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.home_sonho_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            val nomeDoSonhoAtual = GRUPOS_DO_BICHO.firstOrNull { it.numero == uiState.grupoDoSonhoConfirmadoHoje }?.nome
            if (nomeDoSonhoAtual != null) {
                Text(
                    text = stringResource(id = R.string.home_sonho_atual, nomeDoSonhoAtual),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        val grupos = if (uiState.listaDeGruposExpandida) GRUPOS_DO_BICHO else uiState.gruposDoSonhoPreview
        // Wireframe 1d: grid-template-columns:1fr 1fr — 2 colunas fixas, não
        // um wrap que varia conforme o texto de cada item.
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            grupos.chunked(2).forEach { linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    linha.forEach { grupo ->
                        CartaoDePreviaDoGrupo(
                            grupo = grupo,
                            confirmadoHoje = uiState.grupoDoSonhoConfirmadoHoje == grupo.numero,
                            onClick = { onGrupoClick(grupo.numero) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (linha.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        val idDoTextoDeExpandir =
            if (uiState.listaDeGruposExpandida) R.string.home_sonho_ver_menos else R.string.home_sonho_ver_todos
        Text(
            text = stringResource(id = idDoTextoDeExpandir),
            style = MaterialTheme.typography.bodySmall,
            modifier =
                Modifier
                    .clickable(role = Role.Button, onClick = onAlternarListaDeGruposClick)
                    .testTag(TAG_BOTAO_VER_GRUPOS),
        )
    }
}

@Composable
private fun CartaoDePreviaDoGrupo(
    grupo: GrupoDoBicho,
    confirmadoHoje: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(
                    border =
                        BorderStroke(
                            if (confirmadoHoje) 2.dp else 1.dp,
                            NocturneAccent.takeIf { confirmadoHoje } ?: NocturneOutline,
                        ),
                    shape = RoundedCornerShape(8.dp),
                ).clickable(role = Role.Button, onClick = onClick)
                .testTag(tagGrupoDoBicho(grupo.numero))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = grupo.emoji)
        Text(text = "${grupo.numero} ${grupo.nome}", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SecaoPalpites(
    uiState: HomeUiState,
    onExcluirClick: (Long) -> Unit,
    onPalpiteClick: (Long) -> Unit,
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
                CartaoPalpite(
                    palpite = palpite,
                    onExcluirClick = { onExcluirClick(palpite.id) },
                    onClick = { onPalpiteClick(palpite.id) },
                )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descricaoExcluir = stringResource(id = R.string.home_excluir_descricao, palpite.numeroDoDia)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onClick)
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
        if (!palpite.dezenasNovas.isNullOrEmpty()) {
            Text(
                text =
                    pluralStringResource(
                        id = R.plurals.home_diff_dezenas_novas,
                        count = palpite.dezenasNovas.size,
                        palpite.dezenasNovas.size,
                        palpite.dezenasNovas.joinToString(" · ") { "%02d".format(it) },
                    ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
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

@Composable
private fun DialogoCartaoDoSonho(
    grupo: GrupoDoBicho,
    jaConfirmadoHoje: Boolean,
    onConfirmarClick: () -> Unit,
    onFecharClick: () -> Unit,
) {
    val dezenasDoGrupo = dezenasDoGrupoDoBicho(grupo.numero)
    val descricaoFechar = stringResource(id = R.string.home_sonho_card_fechar_descricao)
    val idDoTextoDoBotaoDeConfirmar =
        if (jaConfirmadoHoje) R.string.home_sonho_card_confirmado else R.string.home_sonho_card_confirmar_cta
    Dialog(onDismissRequest = onFecharClick) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, NocturneOutline),
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = grupo.emoji,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(id = R.string.home_sonho_card_grupo_rotulo, grupo.numero),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = "✕",
                        modifier =
                            Modifier
                                .clickable(role = Role.Button, onClick = onFecharClick)
                                .semantics {
                                    contentDescription = descricaoFechar
                                }.padding(start = 12.dp),
                    )
                }
                Text(text = grupo.nome, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stringResource(id = R.string.home_sonho_card_kicker),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(text = grupo.leituraPopular, style = MaterialTheme.typography.bodyMedium)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                            .padding(10.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.home_sonho_card_dezenas_titulo),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = dezenasDoGrupo.joinToString(" · ") { "%02d".format(it) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = stringResource(id = R.string.home_sonho_card_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(id = idDoTextoDoBotaoDeConfirmar),
                    style = MaterialTheme.typography.titleSmall,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(border = BorderStroke(1.dp, NocturneAccent), shape = RoundedCornerShape(8.dp))
                            .then(
                                if (jaConfirmadoHoje) {
                                    Modifier
                                } else {
                                    Modifier.clickable(role = Role.Button, onClick = onConfirmarClick)
                                },
                            ).padding(12.dp),
                )
            }
        }
    }
}
