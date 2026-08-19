package com.trevo.app.navegacao

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.Checks
import com.adamglin.phosphoricons.fill.ClockCounterClockwise
import com.adamglin.phosphoricons.fill.House
import com.adamglin.phosphoricons.fill.User
import com.adamglin.phosphoricons.regular.Checks
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.User
import com.trevo.app.R
import com.trevo.core.ui.BarraDeNavegacaoInferior
import com.trevo.core.ui.ItemDeNavegacao

// Rotas que mostram a barra: as quatro abas de conteúdo, mais Detalhe e
// Desdobramentos (alcançados a partir da Home, sem aba própria — RF-04).
// Onboarding, ritual, geração e folhas/diálogos modais nunca aparecem
// aqui — não são navegação persistente por abas.
val ROTAS_COM_BARRA_DE_NAVEGACAO =
    setOf(Rotas.HOME, Rotas.CONFERENCIA, Rotas.HISTORICO, Rotas.DETALHE, Rotas.DESDOBRAMENTOS)

private fun abaAtivaPara(rota: String?): String? =
    when (rota) {
        Rotas.HOME, Rotas.DETALHE, Rotas.DESDOBRAMENTOS -> "inicio"
        Rotas.CONFERENCIA -> "conferencia"
        Rotas.HISTORICO -> "historico"
        else -> null
    }

@Composable
fun BarraDeNavegacaoDoApp(
    rotaAtual: String?,
    navController: NavHostController,
) {
    val abaAtiva = abaAtivaPara(rotaAtual)

    val rotuloInicio = stringResource(id = R.string.home_nav_inicio)
    val rotuloConferir = stringResource(id = R.string.home_nav_conferir)
    val rotuloHistorico = stringResource(id = R.string.home_nav_historico)
    val rotuloPerfil = stringResource(id = R.string.home_nav_perfil)

    val inicioAtivo = abaAtiva == "inicio"
    val conferirAtivo = abaAtiva == "conferencia"
    val historicoAtivo = abaAtiva == "historico"

    BarraDeNavegacaoInferior(
        itens =
            listOf(
                ItemDeNavegacao(
                    rotulo = rotuloInicio,
                    descricaoDoEstado =
                        if (inicioAtivo) {
                            stringResource(
                                id = R.string.nav_item_descricao_ativo,
                                rotuloInicio,
                            )
                        } else {
                            rotuloInicio
                        },
                    icone = if (inicioAtivo) PhosphorIcons.Fill.House else PhosphorIcons.Regular.House,
                    ativo = inicioAtivo,
                    onClick = {
                        if (!inicioAtivo) navController.popBackStack(Rotas.HOME, false)
                    },
                ),
                ItemDeNavegacao(
                    rotulo = rotuloConferir,
                    descricaoDoEstado =
                        if (conferirAtivo) {
                            stringResource(id = R.string.nav_item_descricao_ativo, rotuloConferir)
                        } else {
                            rotuloConferir
                        },
                    icone = if (conferirAtivo) PhosphorIcons.Fill.Checks else PhosphorIcons.Regular.Checks,
                    ativo = conferirAtivo,
                    onClick = {
                        if (!conferirAtivo) {
                            navController.navigate(Rotas.CONFERENCIA) {
                                launchSingleTop = true
                                popUpTo(Rotas.HOME)
                            }
                        }
                    },
                ),
                ItemDeNavegacao(
                    rotulo = rotuloHistorico,
                    descricaoDoEstado =
                        if (historicoAtivo) {
                            stringResource(id = R.string.nav_item_descricao_ativo, rotuloHistorico)
                        } else {
                            rotuloHistorico
                        },
                    icone =
                        if (historicoAtivo) {
                            PhosphorIcons.Fill.ClockCounterClockwise
                        } else {
                            PhosphorIcons.Regular.ClockCounterClockwise
                        },
                    ativo = historicoAtivo,
                    onClick = {
                        if (!historicoAtivo) {
                            navController.navigate(Rotas.HISTORICO) {
                                launchSingleTop = true
                                popUpTo(Rotas.HOME)
                            }
                        }
                    },
                ),
                ItemDeNavegacao(
                    rotulo = rotuloPerfil,
                    descricaoDoEstado = rotuloPerfil,
                    icone = PhosphorIcons.Regular.User,
                    ativo = false,
                    // RF-07 ainda não existe — aba visível, sem destino.
                    onClick = {},
                ),
            ),
    )
}
