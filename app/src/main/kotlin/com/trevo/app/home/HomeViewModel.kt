package com.trevo.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val FORMATO_HORARIO = DateTimeFormatter.ofPattern("HH:mm")

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val repository: PalpiteRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val palpiteParaConfirmarExclusao = MutableStateFlow<Long?>(null)

        val uiState: StateFlow<HomeUiState> =
            combine(
                repository.observarPalpitesDoDia(LocalDate.now(clock), clock.zone),
                palpiteParaConfirmarExclusao,
            ) { palpites, idParaExcluir ->
                HomeUiState(
                    carregando = false,
                    palpitesHoje = palpites.paraItens(),
                    palpiteParaConfirmarExclusao = idParaExcluir,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )

        private fun List<PalpiteSalvo>.paraItens(): List<PalpiteItemUiState> {
            val total = size
            return mapIndexed { indice, salvo ->
                PalpiteItemUiState(
                    id = salvo.id,
                    numeroDoDia = total - indice,
                    dezenas = salvo.palpite.dezenas,
                    forca = salvo.palpite.forca,
                    // LocalTime.ofInstant só existe a partir da API 31; minSdk
                    // do Trevo é 26 (CLAUDE.md §2/RNF-05.1), daí o caminho por
                    // ZonedDateTime, disponível desde a API 26.
                    horario = ZonedDateTime.ofInstant(salvo.criadoEm, clock.zone).format(FORMATO_HORARIO),
                )
            }
        }

        fun aoPedirExclusao(id: Long) {
            palpiteParaConfirmarExclusao.value = id
        }

        fun aoCancelarExclusao() {
            palpiteParaConfirmarExclusao.value = null
        }

        fun aoConfirmarExclusao() {
            val id = palpiteParaConfirmarExclusao.value ?: return
            viewModelScope.launch { repository.excluir(id) }
            palpiteParaConfirmarExclusao.value = null
        }
    }
