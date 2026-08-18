package com.trevo.app.detalhe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.app.home.CUSTO_POR_JOGO
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.engine.palpite.coeficienteBinomial
import com.trevo.core.engine.palpite.combinacoesDe15
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

private const val CHAVE_PALPITE_ID = "palpiteId"
private const val QUANTIDADE_EXIBIDA = 24

@HiltViewModel
class DesdobramentosViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: PalpiteRepository,
    ) : ViewModel() {
        private val palpiteId: Long = checkNotNull(savedStateHandle[CHAVE_PALPITE_ID])

        private val estado = MutableStateFlow(DesdobramentosUiState())
        val uiState: StateFlow<DesdobramentosUiState> = estado.asStateFlow()

        init {
            viewModelScope.launch {
                val palpiteSalvo = repository.observarPalpitePorId(palpiteId).first() ?: return@launch
                val dezenas = palpiteSalvo.palpite.dezenas
                val jogosEquivalentes = coeficienteBinomial(dezenas.size, 15)
                estado.value =
                    DesdobramentosUiState(
                        carregando = false,
                        quantidadeDeDezenas = dezenas.size,
                        jogosEquivalentes = jogosEquivalentes,
                        custoTotal = CUSTO_POR_JOGO.multiply(BigDecimal(jogosEquivalentes)),
                        combinacoesExibidas = combinacoesDe15(dezenas).take(QUANTIDADE_EXIBIDA).toList(),
                    )
            }
        }
    }
