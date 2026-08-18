package com.trevo.app.detalhe

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trevo.app.R
import com.trevo.core.ui.NocturneOutline
import com.trevo.core.ui.NocturneSurface
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TelaDesdobramentos(
    uiState: DesdobramentosUiState,
    onVoltarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val descricaoVoltar = stringResource(id = R.string.detalhe_voltar_descricao)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "←",
                    modifier =
                        Modifier
                            .clickable(role = Role.Button, onClick = onVoltarClick)
                            .semantics { contentDescription = descricaoVoltar },
                )
                Text(
                    text = stringResource(id = R.string.desdobramentos_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            if (!uiState.carregando) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.desdobramentos_explicacao, uiState.quantidadeDeDezenas),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.desdobramentos_jogos_titulo),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = uiState.jogosEquivalentes.toString(),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(id = R.string.desdobramentos_custo_titulo),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(text = formatarReais(uiState.custoTotal), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                Text(
                    text =
                        stringResource(
                            id = R.string.desdobramentos_mostrando,
                            uiState.combinacoesExibidas.size,
                            uiState.jogosEquivalentes,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                )
                uiState.combinacoesExibidas.forEachIndexed { indice, combinacao ->
                    CartaoDeCombinacao(numero = indice + 1, dezenas = combinacao)
                }
            }
        }
    }
}

@Composable
private fun CartaoDeCombinacao(
    numero: Int,
    dezenas: List<Int>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = NocturneSurface, shape = RoundedCornerShape(8.dp))
                .border(border = BorderStroke(1.dp, NocturneOutline), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = numero.toString(), style = MaterialTheme.typography.bodySmall)
        Text(text = dezenas.joinToString(" · ") { "%02d".format(it) }, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatarReais(valor: BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
