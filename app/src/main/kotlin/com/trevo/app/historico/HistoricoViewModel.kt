package com.trevo.app.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.app.home.CUSTO_POR_JOGO
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.palpite.coeficienteBinomial
import com.trevo.core.engine.resultado.Resultado
import com.trevo.core.engine.resultado.conferir
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private const val CONCURSOS_POR_PAGINA = 3

@HiltViewModel
class HistoricoViewModel
    @Inject
    constructor(
        palpiteRepository: PalpiteRepository,
        resultadoRepository: ResultadoRepository,
        clock: Clock,
    ) : ViewModel() {
        private val concursosRevelados = MutableStateFlow(CONCURSOS_POR_PAGINA)

        val uiState: StateFlow<HistoricoUiState> =
            combine(
                palpiteRepository.observarTodosOsPalpites(),
                resultadoRepository.observarTodosOsResultados(),
                concursosRevelados,
            ) { palpites, resultados, revelados ->
                montarUiState(palpites, resultados, revelados, clock.zone)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoricoUiState.Carregando,
            )

        fun aoVerMaisClick() {
            concursosRevelados.value += CONCURSOS_POR_PAGINA
        }

        private fun montarUiState(
            palpites: List<PalpiteSalvo>,
            resultados: List<Resultado>,
            revelados: Int,
            zona: ZoneId,
        ): HistoricoUiState {
            // RF-06.1 — só concursos já conferidos: um dia de palpites sem
            // resultado casado ainda não sorteou/foi buscado, e CLAUDE.md
            // §8 proíbe inventar essa associação.
            val concursos =
                palpites
                    .groupBy { it.criadoEm.atZone(zona).toLocalDate() }
                    .mapNotNull { (dia, palpitesDoDia) ->
                        val resultado = resultados.firstOrNull { it.dataApuracao == dia } ?: return@mapNotNull null
                        montarConcurso(dia, palpitesDoDia, resultado)
                    }.sortedByDescending { it.data }

            if (concursos.isEmpty()) return HistoricoUiState.Vazio

            val todosOsPalpites = concursos.flatMap { it.palpites }
            val totalGasto = concursos.sumCustoTotal()
            val totalGanho = todosOsPalpites.fold(BigDecimal.ZERO) { acc, p -> acc + (p.premio ?: BigDecimal.ZERO) }
            val retorno =
                if (totalGasto.signum() == 0) {
                    0
                } else {
                    totalGanho
                        .divide(totalGasto, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal(100))
                        .toInt()
                }

            return HistoricoUiState.ComDados(
                totalDeJogos = todosOsPalpites.size,
                totalDeConcursos = concursos.size,
                totalGasto = totalGasto,
                totalGanho = totalGanho,
                saldo = totalGanho - totalGasto,
                retornoPercentual = retorno,
                mediaGastoPorConcurso = totalGasto.divide(BigDecimal(concursos.size), 2, RoundingMode.HALF_UP),
                melhorResultadoEmAcertos = todosOsPalpites.maxOf { it.acertos },
                faixas =
                    (15 downTo 11).map { acertos ->
                        FaixaHistoricoUiState(acertos, todosOsPalpites.count { it.acertos == acertos })
                    },
                concursosRevelados = concursos.take(revelados),
                temMaisConcursos = concursos.size > revelados,
                quantidadeDeConcursosRestantes = (concursos.size - revelados).coerceAtLeast(0),
            )
        }

        private fun montarConcurso(
            dia: LocalDate,
            palpitesDoDia: List<PalpiteSalvo>,
            resultado: Resultado,
        ): ConcursoConferidoUiState {
            // Do mais antigo pro mais novo — mesma convenção de numeroDoDia
            // usada em Home/Detalhe/Conferência (1 = primeiro do dia).
            val palpites =
                palpitesDoDia.reversed().mapIndexed { indice, salvo ->
                    val conferencia = conferir(salvo.palpite, resultado)
                    PalpiteNoHistoricoUiState(
                        numeroDoDia = indice + 1,
                        dezenas = salvo.palpite.dezenas,
                        acertos = conferencia.acertos,
                        premio = conferencia.faixa?.valorPremio,
                    )
                }
            val premioTotal = palpites.fold(BigDecimal.ZERO) { acc, p -> acc + (p.premio ?: BigDecimal.ZERO) }
            return ConcursoConferidoUiState(
                numero = resultado.numero,
                data = dia,
                premioTotal = premioTotal,
                palpites = palpites,
            )
        }

        private fun List<ConcursoConferidoUiState>.sumCustoTotal(): BigDecimal =
            flatMap { it.palpites }.fold(BigDecimal.ZERO) { acc, palpite ->
                acc + CUSTO_POR_JOGO.multiply(BigDecimal(coeficienteBinomial(palpite.dezenas.size, 15)))
            }
    }
