package com.trevo.app.geracao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val INTERVALO_ENTRE_FRASES_MS = 650L
private const val INTERVALO_ENTRE_FRASES_REDUZIDO_MS = 50L

@HiltViewModel
class GeracaoViewModel
    @Inject
    constructor() : ViewModel() {
        private val estado = MutableStateFlow(GeracaoUiState())
        val uiState: StateFlow<GeracaoUiState> = estado.asStateFlow()
        private var iniciado = false

        // O palpite já foi gerado e salvo antes desta tela abrir
        // (CrencasViewModel.aoGerarPalpite) — RNF-01.2 garante que os <100ms
        // de cálculo real não são o motivo da espera. A espera aqui é
        // encenação: "o tempo de espera é o ritual, não latência real"
        // (wireframe 1f). `movimentoReduzido` vem da preferência de sistema
        // lida na Composable — o ViewModel não toca em Settings do Android.
        fun iniciar(movimentoReduzido: Boolean) {
            if (iniciado) return
            iniciado = true
            val intervalo = if (movimentoReduzido) INTERVALO_ENTRE_FRASES_REDUZIDO_MS else INTERVALO_ENTRE_FRASES_MS
            viewModelScope.launch {
                for (indice in 0 until QUANTIDADE_DE_FRASES_DO_RITUAL) {
                    estado.value = estado.value.copy(indiceFrase = indice)
                    delay(intervalo)
                }
                estado.value = estado.value.copy(concluido = true)
            }
        }
    }
