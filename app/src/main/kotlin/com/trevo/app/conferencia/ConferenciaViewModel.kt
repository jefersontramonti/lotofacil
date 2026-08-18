package com.trevo.app.conferencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.app.home.CUSTO_POR_JOGO
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.palpite.coeficienteBinomial
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import com.trevo.core.engine.resultado.conferir
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ConferenciaViewModel
    @Inject
    constructor(
        private val resultadoRepository: ResultadoRepository,
        private val palpiteRepository: PalpiteRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val estado = MutableStateFlow<ConferenciaUiState>(ConferenciaUiState.Carregando)
        val uiState: StateFlow<ConferenciaUiState> = estado.asStateFlow()

        fun aoEntrar() = buscar()

        fun aoTentarNovamente() = buscar()

        fun aoInformarResultadoManualmente(dezenasSorteadas: Set<Int>) {
            viewModelScope.launch {
                resultadoRepository.salvarResultadoManual(dezenasSorteadas)
                buscar()
            }
        }

        // RF-05.3 — busca (e confere) toda vez que a tela abre. RF-05.8/05.9
        // distinguem sem-rede (IOException) de falha do serviço (qualquer
        // outra exceção — o tipo exato de erro HTTP é detalhe de transporte
        // de :core:data, o ViewModel não precisa conhecê-lo) pelo tipo da
        // exceção, igual ao contrato de ResultadoRepository.buscarUltimoResultado().
        private fun buscar() {
            estado.value = ConferenciaUiState.Carregando
            viewModelScope.launch {
                val resultado =
                    try {
                        resultadoRepository.buscarUltimoResultado()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: IOException) {
                        estado.value = ConferenciaUiState.SemConexao
                        return@launch
                    } catch (e: Exception) {
                        estado.value = ConferenciaUiState.Falha
                        return@launch
                    }
                val hoje = LocalDate.now(clock)
                val palpitesHoje = palpiteRepository.observarPalpitesDoDia(hoje, clock.zone).first()
                estado.value = montarResultado(resultado, hoje, palpitesHoje)
            }
        }

        private fun montarResultado(
            resultado: Resultado,
            hoje: LocalDate,
            palpitesHoje: List<PalpiteSalvo>,
        ): ConferenciaUiState {
            if (palpitesHoje.isNotEmpty() && resultado.dataApuracao.isBefore(hoje)) {
                return ConferenciaUiState.Espera
            }

            // Do mais antigo pro mais novo — mesma convenção de numeroDoDia
            // usada em HomeViewModel/DetalheViewModel (1 = primeiro do dia).
            val itens =
                palpitesHoje.reversed().mapIndexed { indice, salvo ->
                    val conferencia = conferir(salvo.palpite, resultado)
                    PalpiteConferidoUiState(
                        numeroDoDia = indice + 1,
                        dezenas = salvo.palpite.dezenas,
                        dezenasAcertadas =
                            salvo.palpite.dezenas
                                .filter { it in resultado.dezenasSorteadas }
                                .toSet(),
                        acertos = conferencia.acertos,
                        premio = conferencia.faixa?.valorPremio,
                    )
                }
            val totalGanho =
                itens.fold(BigDecimal.ZERO) { acumulado, item ->
                    acumulado +
                        (item.premio ?: BigDecimal.ZERO)
                }
            val totalGasto =
                itens.fold(BigDecimal.ZERO) { acumulado, item ->
                    acumulado + CUSTO_POR_JOGO.multiply(BigDecimal(coeficienteBinomial(item.dezenas.size, 15)))
                }

            return ConferenciaUiState.Sucesso(
                numeroDoConcurso = resultado.numero,
                dezenasSorteadas = resultado.dezenasSorteadas,
                totalGanho = totalGanho,
                totalGasto = totalGasto,
                itens = itens,
                origemManual = resultado.origem == OrigemDoResultado.MANUAL,
            )
        }
    }
