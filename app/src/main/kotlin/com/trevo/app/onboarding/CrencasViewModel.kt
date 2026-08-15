package com.trevo.app.onboarding

import androidx.lifecycle.ViewModel
import com.trevo.core.engine.crenca.Crenca
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CrencasViewModel
    @Inject
    constructor() : ViewModel() {
        private val estado = MutableStateFlow(CrencasUiState())
        val uiState: StateFlow<CrencasUiState> = estado.asStateFlow()

        fun aoTocarCrenca(crenca: Crenca) {
            val selecaoAtual = estado.value.selecionadas
            val novaSelecao = if (crenca in selecaoAtual) selecaoAtual - crenca else selecaoAtual + crenca
            estado.value = estado.value.copy(selecionadas = novaSelecao)
        }
    }
