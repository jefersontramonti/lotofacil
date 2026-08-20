package com.trevo.app.ritual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import com.trevo.core.engine.crenca.ModoDeGeracao
import com.trevo.core.engine.crenca.ORDEM_DO_RITUAL
import com.trevo.core.engine.crenca.OpcaoDeAmuleto
import com.trevo.core.engine.crenca.RevelacaoDoAmuleto
import com.trevo.core.engine.palpite.PalpiteGenerator
import com.trevo.core.engine.palpite.TamanhoDeFechamento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

// RF-02.8/RF-11 — 15 é o tamanho padrão fora de fechamento; DEZESSEIS/
// DEZOITO/VINTE são fechamento, hoje sempre bloqueados no ritual porque
// `isPro` nunca é true (RF-09 ainda não existe — mesma pendência já
// registrada em RF-01.8/RF-04.10).
private const val QUANTIDADE_PADRAO_DO_PALPITE = 15

sealed interface RitualEvento {
    data object PalpiteMontado : RitualEvento
}

// RF-11 — estado local, nunca persistido: sair da tela (fechar ou montar o
// palpite) destrói este ViewModel e com ele qualquer revelação em
// andamento. É essa efemeridade que garante RF-11.11 (limpar o ritual após
// a montagem, sem reaproveitar dezenas do ritual anterior) — o bug real do
// protótipo que CLAUDE.md §4 documenta era exatamente reter esse estado.
@HiltViewModel
class RitualViewModel
    @Inject
    constructor(
        private val gerador: PalpiteGenerator,
        private val preferenciasRepository: PreferenciasRepository,
        private val palpiteRepository: PalpiteRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val estado = MutableStateFlow<RitualUiState>(RitualUiState.Carregando)
        val uiState: StateFlow<RitualUiState> = estado.asStateFlow()

        private val canalDeEventos = Channel<RitualEvento>(Channel.BUFFERED)
        val eventos: Flow<RitualEvento> = canalDeEventos.receiveAsFlow()

        private var crencasAtivas: Set<Crenca> = emptySet()
        private lateinit var dados: DadosDeContribuicao

        init {
            viewModelScope.launch {
                val hoje = LocalDate.now(clock)
                val perfil = preferenciasRepository.observarPerfil().first()
                val grupoDoSonho = preferenciasRepository.observarGrupoDoSonhoDeHoje(hoje).first()
                crencasAtivas = perfil?.crencasAtivas ?: emptySet()
                dados =
                    DadosDeContribuicao(
                        hoje = hoje,
                        nascimento = perfil?.nascimento,
                        signo = perfil?.signo,
                        nome = perfil?.nome,
                        grupoDoSonho = grupoDoSonho,
                    )
                estado.value = primeiroPasso()
            }
        }

        private fun primeiroPasso(): RitualUiState.Escolha =
            RitualUiState.Escolha(
                amuletoAtual = ORDEM_DO_RITUAL[0],
                indice = 1,
                total = ORDEM_DO_RITUAL.size,
                reveladas = emptyList(),
            )

        // RF-11.5/RF-11.7 — a dezena só existe a partir daqui, nunca antes.
        fun aoEscolherOpcao(opcao: OpcaoDeAmuleto) {
            val atual = estado.value as? RitualUiState.Escolha ?: return
            val dezenasReveladas = atual.reveladas.map { it.dezena }.toSet()
            val dezena = gerador.sortearDezenaDoRitual(crencasAtivas, dados, dezenasReveladas)
            val revelacao = RevelacaoDoAmuleto(atual.amuletoAtual, opcao, dezena)
            estado.value = RitualUiState.Revelando(revelacao, atual.reveladas + revelacao)
        }

        // Chamado pela Composable depois da animação de revelação (RF-11.6) —
        // "avança sozinho para o próximo amuleto" (wireframe 1r).
        fun aoRevelacaoTerminou() {
            val atual = estado.value as? RitualUiState.Revelando ?: return
            val proximoIndice = atual.reveladas.size
            estado.value =
                if (proximoIndice < ORDEM_DO_RITUAL.size) {
                    RitualUiState.Escolha(
                        ORDEM_DO_RITUAL[proximoIndice],
                        proximoIndice + 1,
                        ORDEM_DO_RITUAL.size,
                        atual.reveladas,
                    )
                } else {
                    RitualUiState.Resumo(
                        reveladas = atual.reveladas,
                        quantidadeDeOutrasDezenas = QUANTIDADE_PADRAO_DO_PALPITE - atual.reveladas.size,
                    )
                }
        }

        fun aoRefazerRitualClick() {
            estado.value = primeiroPasso()
        }

        // RF-02.8 — a UI (TelaRitual) só chama isto pra um tamanho
        // desbloqueado; um tamanho de fechamento com `isPro == false` vai
        // pra `onTamanhoBloqueadoClick` em vez de aqui — mesma divisão de
        // responsabilidade de TelaCrencas.onCrencaClick/onCrencaBloqueadaClick.
        fun aoEscolherTamanho(tamanho: TamanhoDeFechamento?) {
            val atual = estado.value as? RitualUiState.Resumo ?: return
            val quantidade = tamanho?.quantidade ?: QUANTIDADE_PADRAO_DO_PALPITE
            estado.value = atual.copy(tamanho = tamanho, quantidadeDeOutrasDezenas = quantidade - atual.reveladas.size)
        }

        // RF-11.9/RF-11.10 — força as dezenas reveladas no volante final
        // (mesmo mecanismo de dezenasFixas do RF-02.2/RF-04.7) e registra o
        // ritual como fonte própria, ao lado das crenças. `tamanho` escolhe
        // entre o palpite padrão de 15 e um fechamento (RF-02.8).
        fun aoMontarPalpiteClick() {
            val atual = estado.value as? RitualUiState.Resumo ?: return
            if (atual.montandoPalpite) return
            estado.value = atual.copy(montandoPalpite = true)
            viewModelScope.launch {
                val palpite =
                    gerador.gerar(
                        crencasAtivas = crencasAtivas,
                        dados = dados,
                        dezenasFixas = atual.reveladas.map { it.dezena }.toSet(),
                        quantidade = atual.tamanho?.quantidade ?: QUANTIDADE_PADRAO_DO_PALPITE,
                        modo = ModoDeGeracao.DESTINO,
                        ritual = atual.reveladas,
                    )
                palpiteRepository.salvar(palpite)
                canalDeEventos.send(RitualEvento.PalpiteMontado)
            }
        }
    }
