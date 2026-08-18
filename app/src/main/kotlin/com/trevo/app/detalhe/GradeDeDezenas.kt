package com.trevo.app.detalhe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.ui.NocturneAccent
import com.trevo.core.ui.NocturneSurface

fun tagDezenaNaGrade(dezena: Int): String = "grade_dezena_$dezena"

// RF-04.1/04.5 — grade 5×5 do volante. `dezenasMarcadas` são as que estão
// no palpite; `dezenasFixas` (subconjunto de `dezenasMarcadas`) ganham
// contorno tracejado e, quando `onDezenaClick` não é nulo (modo edição),
// não respondem a toque — fixa é permanente (RF-04.7).
@Composable
fun GradeDeDezenas(
    dezenasMarcadas: Set<Int>,
    dezenasFixas: Set<Int>,
    modifier: Modifier = Modifier,
    onDezenaClick: ((Int) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        (0..4).forEach { linha ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { coluna ->
                    val dezena = linha * 5 + coluna
                    CelulaDaGrade(
                        dezena = dezena,
                        marcada = dezena in dezenasMarcadas,
                        fixa = dezena in dezenasFixas,
                        onClick = onDezenaClick?.let { { it(dezena) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CelulaDaGrade(
    dezena: Int,
    marcada: Boolean,
    fixa: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val idDaDescricao =
        if (marcada) R.string.detalhe_dezena_marcada_descricao else R.string.detalhe_dezena_nao_marcada_descricao
    val descricaoEstado = stringResource(id = idDaDescricao, dezena)
    val corDeFundo =
        when {
            fixa -> NocturneAccent.copy(alpha = 0.35f)
            marcada -> NocturneAccent
            else -> NocturneSurface
        }
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .background(color = corDeFundo, shape = RoundedCornerShape(6.dp))
                .then(
                    // Contorno tracejado real não existe nativamente no
                    // Compose sem Canvas dedicado — a borda de destaque
                    // marca "fixa" (RF-04.7) sem essa complexidade extra.
                    if (fixa) {
                        Modifier.border(BorderStroke(1.5.dp, NocturneAccent), RoundedCornerShape(6.dp))
                    } else {
                        Modifier
                    },
                ).then(
                    if (onClick != null && !fixa) {
                        Modifier.clickable(role = Role.Checkbox, onClick = onClick)
                    } else {
                        Modifier
                    },
                ).semantics(mergeDescendants = true) { contentDescription = descricaoEstado }
                .testTag(tagDezenaNaGrade(dezena)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "%02d".format(dezena),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (marcada) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
