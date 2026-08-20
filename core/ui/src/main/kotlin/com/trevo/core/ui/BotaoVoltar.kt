package com.trevo.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Botão de voltar padrão do Trevo — círculo com borda sutil sempre visível
// (NocturneOutline) e ripple no tint roxo do sistema (NocturneAccent) só
// no toque/hover. Alvo de toque de 48dp (CLAUDE.md §6).
@Composable
fun BotaoVoltar(
    onClick: () -> Unit,
    descricao: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(48.dp)
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = NocturneAccent),
                    role = Role.Button,
                    onClick = onClick,
                ).semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "←", style = MaterialTheme.typography.titleMedium)
    }
}
