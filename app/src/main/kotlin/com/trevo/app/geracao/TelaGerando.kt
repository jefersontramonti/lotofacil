package com.trevo.app.geracao

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.ui.NocturneAccent

@Composable
fun TelaGerando(
    uiState: GeracaoUiState,
    modifier: Modifier = Modifier,
    movimentoReduzido: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                IconeDoRitual(movimentoReduzido = movimentoReduzido)
                Text(
                    text = stringResource(id = fraseDe(uiState.indiceFrase)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = stringResource(id = R.string.geracao_subtitulo), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun IconeDoRitual(
    movimentoReduzido: Boolean,
    modifier: Modifier = Modifier,
) {
    val angulo =
        if (movimentoReduzido) {
            0f
        } else {
            val transicaoInfinita = rememberInfiniteTransition(label = "rotacaoDoRitual")
            val anguloAnimado by
                transicaoInfinita.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(durationMillis = 2_000, easing = LinearEasing),
                        ),
                    label = "anguloDoIcone",
                )
            anguloAnimado
        }
    Box(
        modifier =
            modifier
                .size(70.dp)
                .graphicsLayer { rotationZ = angulo }
                .border(border = BorderStroke(2.dp, NocturneAccent), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.trevo),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
    }
}

private fun fraseDe(indice: Int): Int =
    when (indice) {
        0 -> R.string.geracao_frase_1
        1 -> R.string.geracao_frase_2
        2 -> R.string.geracao_frase_3
        else -> R.string.geracao_frase_4
    }
