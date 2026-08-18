package com.trevo.core.ui

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class ItemDeNavegacao(
    val rotulo: String,
    val descricaoDoEstado: String,
    val icone: ImageVector,
    val ativo: Boolean,
    val onClick: () -> Unit,
)

// Barra de navegação persistente por abas — segue o protótipo
// (Trevo - Lotofácil.dc.html): ícone cheio + tint de 14% do acento
// atrás quando ativa, ícone contornado + texto a 72% de opacidade
// quando inativa. RNF-03.5: nunca só por cor — o peso do ícone (cheio
// vs. contornado) já distingue o estado, a cor é reforço.
@Composable
fun BarraDeNavegacaoInferior(
    itens: List<ItemDeNavegacao>,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(color = NocturneOutline)
    NavigationBar(
        modifier = modifier,
        containerColor = NocturneBackground,
        contentColor = NocturneText,
        tonalElevation = 0.dp,
    ) {
        itens.forEach { item ->
            NavigationBarItem(
                selected = item.ativo,
                onClick = item.onClick,
                icon = { Icon(imageVector = item.icone, contentDescription = null) },
                label = { Text(text = item.rotulo) },
                modifier =
                    Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .semantics(mergeDescendants = true) { contentDescription = item.descricaoDoEstado },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = NocturneAccent,
                        selectedTextColor = NocturneAccent,
                        indicatorColor = NocturneAccent.copy(alpha = 0.14f),
                        unselectedIconColor = NocturneText.copy(alpha = 0.72f),
                        unselectedTextColor = NocturneText.copy(alpha = 0.72f),
                    ),
            )
        }
    }
}
