package com.trevo.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import com.trevo.core.data.preferencias.PerfilSalvo
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.GRUPOS_DO_BICHO
import com.trevo.core.engine.crenca.faseDaLuaEm
import com.trevo.core.engine.crenca.indiceDeSorteDoDia
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
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

private val FORMATO_HORARIO = DateTimeFormatter.ofPattern("HH:mm")
private const val QUANTIDADE_DE_GRUPOS_NA_PREVIA = 4

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val repository: PalpiteRepository,
        private val preferenciasRepository: PreferenciasRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val palpiteParaConfirmarExclusao = MutableStateFlow<Long?>(null)
        private val numeroDoGrupoAbertoNoDialog = MutableStateFlow<Int?>(null)
        private val listaDeGruposExpandida = MutableStateFlow(false)

        private data class EstadoLocal(
            val idParaExcluir: Long?,
            val numeroDoGrupoAberto: Int?,
            val listaExpandida: Boolean,
        )

        private val estadoLocal =
            combine(
                palpiteParaConfirmarExclusao,
                numeroDoGrupoAbertoNoDialog,
                listaDeGruposExpandida,
            ) { idParaExcluir, numeroDoGrupoAberto, listaExpandida ->
                EstadoLocal(idParaExcluir, numeroDoGrupoAberto, listaExpandida)
            }

        val uiState: StateFlow<HomeUiState> =
            combine(
                repository.observarPalpitesDoDia(LocalDate.now(clock), clock.zone),
                preferenciasRepository.observarPerfil(),
                preferenciasRepository.observarGrupoDoSonhoDeHoje(LocalDate.now(clock)),
                estadoLocal,
            ) { palpites, perfil, grupoConfirmado, local ->
                montarUiState(palpites, perfil, grupoConfirmado, local)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )

        private fun montarUiState(
            palpites: List<PalpiteSalvo>,
            perfil: PerfilSalvo?,
            grupoConfirmado: Int?,
            local: EstadoLocal,
        ): HomeUiState {
            val hoje = LocalDate.now(clock)
            return HomeUiState(
                carregando = false,
                palpitesHoje = palpites.paraItens(),
                palpiteParaConfirmarExclusao = local.idParaExcluir,
                nome = perfil?.nome,
                indiceDeSorte = perfil?.let { indiceDeSorteDoDia(it.nome, hoje) },
                faseDaLua = perfil?.let { faseDaLuaEm(hoje) },
                signo = perfil?.signo,
                diaDaSemanaAbreviado = perfil?.let { diaDaSemanaAbreviadoDe(hoje) },
                crencaSonhoAtiva = perfil?.crencasAtivas?.contains(Crenca.SONHO) == true,
                gruposDoSonhoPreview = GRUPOS_DO_BICHO.take(QUANTIDADE_DE_GRUPOS_NA_PREVIA),
                listaDeGruposExpandida = local.listaExpandida,
                grupoDoSonhoConfirmadoHoje = grupoConfirmado,
                grupoAbertoNoDialog = local.numeroDoGrupoAberto?.let { grupoDoBichoDeNumero(it) },
            )
        }

        private fun grupoDoBichoDeNumero(numero: Int) = GRUPOS_DO_BICHO.firstOrNull { it.numero == numero }

        private fun diaDaSemanaAbreviadoDe(data: LocalDate): String =
            data.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))

        // A lista já vem do repositório ordenada do mais recente pro mais
        // antigo (RF-03.4) — RF-03.5 compara cada palpite com o próximo da
        // lista, que é o "imediatamente anterior" no tempo.
        private fun List<PalpiteSalvo>.paraItens(): List<PalpiteItemUiState> {
            val total = size
            return mapIndexed { indice, salvo ->
                val anterior = getOrNull(indice + 1)
                PalpiteItemUiState(
                    id = salvo.id,
                    numeroDoDia = total - indice,
                    dezenas = salvo.palpite.dezenas,
                    forca = salvo.palpite.forca,
                    // LocalTime.ofInstant só existe a partir da API 31; minSdk
                    // do Trevo é 26 (CLAUDE.md §2/RNF-05.1), daí o caminho por
                    // ZonedDateTime, disponível desde a API 26.
                    horario = ZonedDateTime.ofInstant(salvo.criadoEm, clock.zone).format(FORMATO_HORARIO),
                    dezenasNovas = anterior?.let { salvo.palpite.dezenas.filterNot { d -> d in it.palpite.dezenas } },
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

        fun aoAlternarListaDeGrupos() {
            listaDeGruposExpandida.value = !listaDeGruposExpandida.value
        }

        fun aoAbrirGrupo(numero: Int) {
            numeroDoGrupoAbertoNoDialog.value = numero
        }

        fun aoFecharDialogDoSonho() {
            numeroDoGrupoAbertoNoDialog.value = null
        }

        fun aoConfirmarSonho(numero: Int) {
            viewModelScope.launch { preferenciasRepository.confirmarGrupoDoSonho(numero, LocalDate.now(clock)) }
            numeroDoGrupoAbertoNoDialog.value = null
        }
    }
