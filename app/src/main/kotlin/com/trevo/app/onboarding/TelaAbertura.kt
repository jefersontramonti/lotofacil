package com.trevo.app.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.ui.BotaoPrimario
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneOutline

@Composable
fun TelaAbertura(
    onComecarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Surface fornece LocalContentColor (onBackground) para toda a árvore,
    // então nenhum Text precisa declarar `color` individualmente. O fundo
    // preenche a janela inteira (edge-to-edge), mas safeDrawingPadding()
    // recolhe as constraints ANTES do BoxWithConstraints medir
    // alturaDisponivel, senão status bar e navigation bar entram no cálculo
    // de centralização e no alvo de toque do botão.
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            val alturaDisponivel = maxHeight
            val density = LocalDensity.current
            // Estimativa inicial da altura do BotaoPrimario em fonte padrão
            // (48dp mínimo + 16dp de padding vertical de cada lado); o
            // onSizeChanged abaixo corrige no frame seguinte com a altura
            // real, inclusive a 200% de fontScale, sem o que o bloco de
            // conteúdo não conseguiria centralizar corretamente.
            var alturaBotao by remember { mutableStateOf(80.dp) }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .defaultMinSize(
                                minHeight = (alturaDisponivel - alturaBotao).coerceAtLeast(0.dp),
                            ).padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .border(
                                    border = BorderStroke(width = 1.5.dp, color = NocturneAccent),
                                    shape = RoundedCornerShape(10.dp),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.trevo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(id = R.string.abertura_proposta),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = NocturneOutline,
                    )
                    Text(
                        text = stringResource(id = R.string.abertura_aviso_aleatoriedade),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                BotaoPrimario(
                    texto = stringResource(id = R.string.abertura_cta_comecar),
                    onClick = onComecarClick,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            // onSizeChanged precisa envolver o padding: senão mede só o
                            // OutlinedButton (48dp) e ignora os 32dp de padding vertical,
                            // subestimando alturaBotao e empurrando o botão para fora da
                            // tela (o defaultMinSize do conteúdo reserva espaço demais).
                            .onSizeChanged { alturaBotao = with(density) { it.height.toDp() } }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
    }
}
