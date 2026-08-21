package com.trevo.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.assinatura.AssinaturaRepository
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import com.trevo.core.data.preferencias.PerfilSalvo
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import com.trevo.core.engine.crenca.GRUPOS_DO_BICHO
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.crencasAtivasNoModo
import com.trevo.core.engine.crenca.faseDaLuaEm
import com.trevo.core.engine.crenca.indiceDeSorteDoDia
import com.trevo.core.engine.palpite.PalpiteGenerator
import com.trevo.core.engine.resultado.Resultado
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

// Docs/Trevo - Lotofácil.dc.html (protótipo de referência, linha 2322):
// `BICHOS.slice(0, 10)` — a prévia recolhida mostra os 10 primeiros grupos.
private const val QUANTIDADE_DE_GRUPOS_NA_PREVIA = 10

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val repository: PalpiteRepository,
        private val preferenciasRepository: PreferenciasRepository,
        private val assinaturaRepository: AssinaturaRepository,
        private val resultadoRepository: ResultadoRepository,
        private val gerador: PalpiteGenerator,
        private val clock: Clock,
    ) : ViewModel() {
        private val palpiteParaConfirmarExclusao = MutableStateFlow<Long?>(null)
        private val numeroDoGrupoAbertoNoDialog = MutableStateFlow<Int?>(null)
        private val listaDeGruposExpandida = MutableStateFlow(false)

        // RF-11.1 — seleção de modo é transiente (não sobrevive a reabrir a Home).
        private val modoSelecionado = MutableStateFlow(ModoDeGeracao.MISTICO)

        private data class EstadoLocal(
            val idParaExcluir: Long?,
            val numeroDoGrupoAberto: Int?,
            val listaExpandida: Boolean,
            val modo: ModoDeGeracao,
        )

        private val estadoLocal =
            combine(
                palpiteParaConfirmarExclusao,
                numeroDoGrupoAbertoNoDialog,
                listaDeGruposExpandida,
                modoSelecionado,
            ) { idParaExcluir, numeroDoGrupoAberto, listaExpandida, modo ->
                EstadoLocal(idParaExcluir, numeroDoGrupoAberto, listaExpandida, modo)
            }

        val uiState: StateFlow<HomeUiState> =
            combine(
                repository.observarPalpitesDoDia(LocalDate.now(clock), clock.zone),
                preferenciasRepository.observarPerfil(),
                preferenciasRepository.observarGrupoDoSonhoDeHoje(LocalDate.now(clock)),
                combine(
                    assinaturaRepository.observarIsPro(),
                    preferenciasRepository.observarPalpitesGratisRestantesHoje(LocalDate.now(clock)),
                    resultadoRepository.observarUltimoResultadoSalvo(),
                    ::Triple,
                ),
                estadoLocal,
            ) { palpites, perfil, grupoConfirmado, proLimiteEResultado, local ->
                val (isPro, restantes, ultimoResultado) = proLimiteEResultado
                montarUiState(palpites, perfil, grupoConfirmado, isPro, restantes, ultimoResultado, local)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )

        private fun montarUiState(
            palpites: List<PalpiteSalvo>,
            perfil: PerfilSalvo?,
            grupoConfirmado: Int?,
            isPro: Boolean,
            palpitesGratisRestantesHoje: Int,
            ultimoResultado: Resultado?,
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
                modoSelecionado = local.modo,
                isPro = isPro,
                palpitesGratisRestantesHoje = palpitesGratisRestantesHoje,
                // RF-03.1 — a Caixa nunca pula número (mesmo sem sorteio aos
                // domingos, Docs/tabelavalores.md), então "último + 1" é o
                // concurso correntemente aceitando apostas. CLAUDE.md §8 proíbe
                // inventar dado de SORTEIO (dezenas/prêmio) — isto é aritmética
                // sobre um número real já confirmado pela API, não um resultado
                // inventado; `null` até o primeiro resultado real ser buscado
                // (RF-05) ou quando o último salvo foi entrada manual sem
                // número (RF-05.10).
                numeroDoConcursoCorrente = ultimoResultado?.numero?.plus(1),
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
                    modo = salvo.palpite.modo,
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

        fun aoSelecionarModo(modo: ModoDeGeracao) {
            modoSelecionado.value = modo
        }

        // RF-11.1/RF-11.2/RF-11.3 — Místico e Cientista geram direto por
        // aqui; Destino troca este botão pelo ritual (ver TrevoNavHost), que
        // chama PalpiteRepository.salvar diretamente ao final, não este método.
        // RF-09.1 — a UI (TelaHome) só mostra este CTA quando
        // `!uiState.semPalpiteGratisHoje`; o guard abaixo é defensivo contra
        // um clique com estado desatualizado, mesmo padrão de
        // DetalheViewModel.aoSalvarEdicao.
        fun aoGerarClick() {
            viewModelScope.launch {
                val perfil = preferenciasRepository.observarPerfil().first() ?: return@launch
                val hoje = LocalDate.now(clock)
                val isPro = assinaturaRepository.observarIsPro().first()
                if (!isPro &&
                    preferenciasRepository.observarPalpitesGratisRestantesHoje(hoje).first() <= 0
                ) {
                    return@launch
                }
                val grupoDoSonho = preferenciasRepository.observarGrupoDoSonhoDeHoje(hoje).first()
                val dados =
                    DadosDeContribuicao(
                        hoje = hoje,
                        nascimento = perfil.nascimento,
                        signo = perfil.signo,
                        nome = perfil.nome,
                        grupoDoSonho = grupoDoSonho,
                    )
                val modo = modoSelecionado.value
                val palpite =
                    gerador.gerar(
                        crencasAtivas = crencasAtivasNoModo(modo, perfil.crencasAtivas),
                        dados = dados,
                        modo = modo,
                    )
                repository.salvar(palpite)
                if (!isPro) preferenciasRepository.registrarPalpiteGratisUsado(hoje)
            }
        }

        // RF-09.2 — chamado depois que AnuncioRecompensadoManager confirma a
        // recompensa (usuário assistiu até o fim); nunca antes.
        fun aoAnuncioRecompensado() {
            viewModelScope.launch { preferenciasRepository.registrarAnuncioAssistido(LocalDate.now(clock)) }
        }
    }
