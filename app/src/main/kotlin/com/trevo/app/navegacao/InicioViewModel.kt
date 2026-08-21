package com.trevo.app.navegacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.preferencias.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// RF-01.2 — decide se o app abre direto na Home (perfil já salvo, nome
// preenchido em `PreferenciasRepository.salvarPerfil`) ou no onboarding
// (primeira vez). `null` enquanto o DataStore ainda não respondeu a
// primeira leitura; `TrevoNavHost` espera esse valor antes de escolher o
// startDestination, senão o app sempre abriria em Abertura e o onboarding
// pediria nome/nascimento de novo a cada vez, mesmo com o perfil salvo.
@HiltViewModel
class InicioViewModel
    @Inject
    constructor(
        preferenciasRepository: PreferenciasRepository,
    ) : ViewModel() {
        val perfilJaExiste: StateFlow<Boolean?> =
            preferenciasRepository
                .observarPerfil()
                .map { it != null }
                .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }
