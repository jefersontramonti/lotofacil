package com.trevo.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NocturneColorScheme =
    darkColorScheme(
        background = NocturneBackground,
        surface = NocturneSurface,
        onBackground = NocturneText,
        onSurface = NocturneText,
        primary = NocturneAccent,
        onPrimary = NocturneBackground,
        secondary = NocturneAccentMuted,
        outline = NocturneOutline,
        error = NocturneError,
    )

// Tema escuro fixo — CLAUDE.md seção 6: "tema escuro é o padrão", não
// segue o tema do sistema.
@Composable
fun TrevoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NocturneColorScheme,
        content = content,
    )
}
