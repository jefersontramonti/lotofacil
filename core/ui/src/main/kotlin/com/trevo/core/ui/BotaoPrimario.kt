package com.trevo.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Botão primário do Trevo — CLAUDE.md seção 6: contorno de acento sobre
// transparente, nunca preenchido. Alvo de toque mínimo de 48dp.
@Composable
fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        enabled = enabled,
        border = BorderStroke(width = 1.dp, color = NocturneAccent),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = NocturneAccent,
            ),
    ) {
        Text(text = texto)
    }
}
