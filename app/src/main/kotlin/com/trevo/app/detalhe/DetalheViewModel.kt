package com.trevo.app.detalhe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.app.home.CUSTO_POR_JOGO
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.crenca.DEZENAS_DA_MOLDURA
import com.trevo.core.engine.crenca.DadosDeContribuicao
import com.trevo.core.engine.palpite.PalpiteGenerator
import com.trevo.core.engine.palpite.coeficienteBinomial
import com.trevo.core.engine.palpite.probabilidadeDe15Acertos
import com.trevo.core.engine.resultado.Resultado
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToLong

private const val CHAVE_PALPITE_ID = "palpiteId"

@HiltViewModel
class DetalheViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: PalpiteRepository,
        private val preferenciasRepository: PreferenciasRepository,
        private val resultadoRepository: ResultadoRepository,
        private val gerador: PalpiteGenerator,
        private val clock: Clock,
    ) : ViewModel() {
        private val palpiteId: Long = checkNotNull(savedStateHandle[CHAVE_PALPITE_ID])

        private val modoEdicao = MutableStateFlow(false)
        private val dezenasEmEdicao = MutableStateFlow<Set<Int>>(emptySet())
        private val guardarComoFixasAoSalvar = MutableStateFlow(false)
        private val palpiteParaConfirmarExclusao = MutableStateFlow(false)
        private val compartilhando = MutableStateFlow(false)
        private val copiado = MutableStateFlow(false)

        private data class EstadoLocal(
            val modoEdicao: Boolean,
            val dezenasEmEdicao: Set<Int>,
            val guardarComoFixasAoSalvar: Boolean,
            val palpiteParaConfirmarExclusao: Boolean,
            val compartilhando: Boolean,
            val copiado: Boolean,
        )

        private val estadoLocal =
            combine(
                modoEdicao,
                dezenasEmEdicao,
                guardarComoFixasAoSalvar,
                palpiteParaConfirmarExclusao,
                combine(compartilhando, copiado, ::Pair),
            ) { edicao, dezenas, guardarFixas, confirmarExclusao, share ->
                EstadoLocal(edicao, dezenas, guardarFixas, confirmarExclusao, share.first, share.second)
            }

        val uiState: StateFlow<DetalheUiState> =
            combine(
                repository.observarPalpitePorId(palpiteId),
                repository.observarPalpitesDoDia(LocalDate.now(clock), clock.zone),
                resultadoRepository.observarTodosOsResultados(),
                estadoLocal,
            ) { palpite, palpitesDoDia, resultados, local ->
                montarUiState(palpite, palpitesDoDia, resultados, local)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetalheUiState(),
            )

        private fun montarUiState(
            palpiteSalvo: PalpiteSalvo?,
            palpitesDoDia: List<PalpiteSalvo>,
            resultados: List<Resultado>,
            local: EstadoLocal,
        ): DetalheUiState {
            if (palpiteSalvo == null) return DetalheUiState(carregando = false, palpiteExiste = false)

            val palpite = palpiteSalvo.palpite
            val quantidadeDeDezenas = palpite.dezenas.size
            val pares = palpite.dezenas.count { it % 2 == 0 }
            val moldura = palpite.dezenas.count { it in DEZENAS_DA_MOLDURA }
            val jogosEquivalentes = coeficienteBinomial(quantidadeDeDezenas, 15)
            val probabilidade = probabilidadeDe15Acertos(quantidadeDeDezenas)
            val indiceNaLista = palpitesDoDia.indexOfFirst { it.id == palpiteId }
            val numeroDoDia = if (indiceNaLista >= 0) palpitesDoDia.size - indiceNaLista else 0
            // RF-08.1/08.2 — só existe quando o concurso do dia do palpite já
            // foi conferido; nunca calculado offline (CLAUDE.md §8).
            val diaDoPalpite = palpiteSalvo.criadoEm.atZone(clock.zone).toLocalDate()
            val numeroDoConcurso = resultados.firstOrNull { it.dataApuracao == diaDoPalpite }?.numero

            return DetalheUiState(
                carregando = false,
                palpiteExiste = true,
                numeroDoDia = numeroDoDia,
                dezenas = palpite.dezenas,
                dezenasFixas = palpite.dezenasFixas,
                forca = palpite.forca,
                origens = palpite.contribuicoes.map { (crenca, dezenas) -> OrigemDeDezenasUiState(crenca, dezenas) },
                origensDoRitual = palpite.ritual,
                modo = palpite.modo,
                soma = palpite.dezenas.sum(),
                pares = pares,
                impares = quantidadeDeDezenas - pares,
                moldura = moldura,
                miolo = quantidadeDeDezenas - moldura,
                custo = CUSTO_POR_JOGO.multiply(BigDecimal(jogosEquivalentes)),
                chanceRealUmEm = (probabilidade.denominador.toDouble() / probabilidade.numerador).roundToLong(),
                quantidadeDeDezenas = quantidadeDeDezenas,
                podeVerDesdobramentos = quantidadeDeDezenas > 15,
                palpiteParaConfirmarExclusao = local.palpiteParaConfirmarExclusao,
                modoEdicao = local.modoEdicao,
                dezenasEmEdicao = local.dezenasEmEdicao,
                guardarComoFixasAoSalvar = local.guardarComoFixasAoSalvar,
                numeroDoConcurso = numeroDoConcurso,
                compartilhando = local.compartilhando,
                copiado = local.copiado,
            )
        }

        fun aoEntrarNoModoEdicao() {
            dezenasEmEdicao.value = uiState.value.dezenas.toSet()
            guardarComoFixasAoSalvar.value = false
            modoEdicao.value = true
        }

        fun aoCancelarEdicao() {
            modoEdicao.value = false
        }

        // RF-04.7: uma dezena já fixa é permanente — não sai marcando/
        // desmarcando fixa durante a edição.
        fun aoTocarDezenaNaEdicao(dezena: Int) {
            if (dezena in uiState.value.dezenasFixas) return
            val atuais = dezenasEmEdicao.value
            dezenasEmEdicao.value = if (dezena in atuais) atuais - dezena else atuais + dezena
        }

        fun aoAlternarGuardarComoFixas() {
            guardarComoFixasAoSalvar.value = !guardarComoFixasAoSalvar.value
        }

        // RF-04.6: só salva com a contagem exata do fechamento.
        fun aoSalvarEdicao() {
            val estadoAtual = uiState.value
            if (estadoAtual.dezenasEmEdicao.size != estadoAtual.quantidadeDeDezenas) return
            modoEdicao.value = false
            viewModelScope.launch {
                val palpiteAtual = repository.observarPalpitePorId(palpiteId).first() ?: return@launch
                val novasFixas =
                    if (estadoAtual.guardarComoFixasAoSalvar) {
                        val dezenasNovas = estadoAtual.dezenasEmEdicao - palpiteAtual.palpite.dezenas.toSet()
                        (palpiteAtual.palpite.dezenasFixas.toSet() + dezenasNovas).sorted()
                    } else {
                        palpiteAtual.palpite.dezenasFixas
                    }
                val palpiteEditado =
                    palpiteAtual.palpite.copy(dezenas = estadoAtual.dezenasEmEdicao.sorted(), dezenasFixas = novasFixas)
                repository.atualizar(palpiteId, palpiteEditado, palpiteAtual.criadoEm)
            }
        }

        fun aoLimparFixas() {
            viewModelScope.launch {
                val palpiteAtual = repository.observarPalpitePorId(palpiteId).first() ?: return@launch
                repository.atualizar(
                    palpiteId,
                    palpiteAtual.palpite.copy(dezenasFixas = emptyList()),
                    palpiteAtual.criadoEm,
                )
            }
        }

        // RF-04.8: refaz mantendo as mesmas crenças ativas (as chaves de
        // contribuicoes — o motor sempre registra uma entrada por crença
        // ativa, contribuindo ou não) e as dezenas fixas atuais.
        fun aoRefazer() {
            viewModelScope.launch {
                val palpiteAtual = repository.observarPalpitePorId(palpiteId).first() ?: return@launch
                val hoje = LocalDate.now(clock)
                val perfil = preferenciasRepository.observarPerfil().first()
                val grupoDoSonho = preferenciasRepository.observarGrupoDoSonhoDeHoje(hoje).first()
                val dados =
                    DadosDeContribuicao(
                        hoje = hoje,
                        nascimento = perfil?.nascimento,
                        signo = perfil?.signo,
                        nome = perfil?.nome,
                        grupoDoSonho = grupoDoSonho,
                    )
                val palpiteRefeito =
                    gerador.gerar(
                        crencasAtivas = palpiteAtual.palpite.contribuicoes.keys,
                        dados = dados,
                        dezenasFixas = palpiteAtual.palpite.dezenasFixas.toSet(),
                        quantidade = palpiteAtual.palpite.dezenas.size,
                        modo = palpiteAtual.palpite.modo,
                        ritual = palpiteAtual.palpite.ritual,
                    )
                repository.atualizar(palpiteId, palpiteRefeito, palpiteAtual.criadoEm)
            }
        }

        fun aoPedirExclusao() {
            palpiteParaConfirmarExclusao.value = true
        }

        fun aoCancelarExclusao() {
            palpiteParaConfirmarExclusao.value = false
        }

        fun aoConfirmarExclusao() {
            viewModelScope.launch { repository.excluir(palpiteId) }
        }

        fun aoAbrirCompartilharClick() {
            copiado.value = false
            compartilhando.value = true
        }

        fun aoFecharCompartilharClick() {
            compartilhando.value = false
            copiado.value = false
        }

        fun aoMarcarCopiadoClick() {
            copiado.value = true
        }
    }
