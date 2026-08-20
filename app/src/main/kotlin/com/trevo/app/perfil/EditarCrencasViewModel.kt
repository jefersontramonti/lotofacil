package com.trevo.app.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.app.onboarding.CrencasUiState
import com.trevo.core.data.assinatura.AssinaturaRepository
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.engine.crenca.Crenca
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// RF-07.2 — "tela dedicada" reaproveita a UI de TelaCrencas do onboarding
// (mesmos cartões, mesmo travamento do grátis em 3), mas este ViewModel só
// altera crencasAtivas: nome/nascimento/signo do perfil já existente ficam
// intocados.
@HiltViewModel
class EditarCrencasViewModel
    @Inject
    constructor(
        private val preferenciasRepository: PreferenciasRepository,
        private val assinaturaRepository: AssinaturaRepository,
    ) : ViewModel() {
        private val estado = MutableStateFlow(CrencasUiState())
        val uiState: StateFlow<CrencasUiState> = estado.asStateFlow()

        init {
            viewModelScope.launch {
                val perfil = preferenciasRepository.observarPerfil().first()
                estado.value = estado.value.copy(selecionadas = perfil?.crencasAtivas ?: emptySet())
            }
            viewModelScope.launch {
                assinaturaRepository.observarIsPro().collect { isPro ->
                    estado.value = estado.value.copy(isPro = isPro)
                }
            }
        }

        fun aoTocarCrenca(crenca: Crenca) {
            val selecaoAtual = estado.value.selecionadas
            val novaSelecao = if (crenca in selecaoAtual) selecaoAtual - crenca else selecaoAtual + crenca
            estado.value = estado.value.copy(selecionadas = novaSelecao)
        }

        fun aoSalvarClick() {
            viewModelScope.launch {
                val perfil = preferenciasRepository.observarPerfil().first() ?: return@launch
                preferenciasRepository.salvarPerfil(
                    nome = perfil.nome,
                    nascimento = perfil.nascimento,
                    signo = perfil.signo,
                    crencasAtivas = estado.value.selecionadas,
                )
            }
        }
    }
