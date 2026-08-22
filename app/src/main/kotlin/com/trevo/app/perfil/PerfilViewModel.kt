package com.trevo.app.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.assinatura.AssinaturaRepository
import com.trevo.core.data.assinatura.EstadoDaAssinatura
import com.trevo.core.data.notificacoes.NotificacoesScheduler
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.preferencias.PerfilSalvo
import com.trevo.core.data.preferencias.PreferenciasDeNotificacao
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.ResultadoDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.identidade.VerificadorDeIdade
import com.trevo.core.engine.identidade.formatarDataNascimento
import com.trevo.core.engine.identidade.signoDe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// RF-07.5 — o wireframe 1m e o texto do requisito dizem "19h", mas isso já
// tinha sido corrigido contra a fonte oficial em RF-03.1/home_horario_apostas
// (Docs/tabelavalores.md: apostas até 20h, sorteio às 21h) — segue o mesmo
// horário corrigido usado em TelaHome, não o do wireframe. Só entra em vigor
// a partir desse horário, nunca antes.
private val HORARIO_FECHAMENTO_DAS_APOSTAS: LocalTime = LocalTime.of(20, 0)
private val FORMATO_CAMPO_NASCIMENTO = DateTimeFormatter.ofPattern("dd/MM/yyyy")

sealed interface PerfilEvento {
    // RF-07.7 — nunca pedida na abertura do app, só aqui, no primeiro toggle ligado.
    data object PedirPermissaoDeNotificacao : PerfilEvento

    // LGPD/achado de auditoria de segurança — perfil e palpites já foram
    // apagados quando este evento chega; quem escuta (TrevoNavHost) navega
    // de volta pro onboarding, já que não existe mais perfil salvo.
    data object DadosExcluidos : PerfilEvento
}

@HiltViewModel
class PerfilViewModel
    @Inject
    constructor(
        private val preferenciasRepository: PreferenciasRepository,
        private val scheduler: NotificacoesScheduler,
        private val validador: ValidadorDataNascimento,
        private val verificador: VerificadorDeIdade,
        private val assinaturaRepository: AssinaturaRepository,
        private val palpiteRepository: PalpiteRepository,
    ) : ViewModel() {
        private val nomeEditado = MutableStateFlow<String?>(null)
        private val nascimentoEditado = MutableStateFlow<String?>(null)

        private val canalDeEventos = Channel<PerfilEvento>(Channel.BUFFERED)
        val eventos: Flow<PerfilEvento> = canalDeEventos.receiveAsFlow()

        private val perfil = preferenciasRepository.observarPerfil()
        private val preferenciasDeNotificacao = preferenciasRepository.observarPreferenciasDeNotificacao()

        private data class EstadoLocal(
            val nome: String?,
            val nascimento: String?,
        )

        private val estadoLocal = combine(nomeEditado, nascimentoEditado, ::EstadoLocal)

        val uiState: StateFlow<PerfilUiState> =
            combine(
                perfil,
                preferenciasDeNotificacao,
                assinaturaRepository.observarAssinatura(),
                estadoLocal,
            ) { perfilSalvo, notificacoes, assinatura, local ->
                montarUiState(perfilSalvo, notificacoes, assinatura, local)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PerfilUiState(),
            )

        private fun montarUiState(
            perfilSalvo: PerfilSalvo?,
            notificacoes: PreferenciasDeNotificacao,
            assinatura: EstadoDaAssinatura,
            local: EstadoLocal,
        ): PerfilUiState {
            val nascimentoTexto = local.nascimento ?: perfilSalvo?.nascimento?.format(FORMATO_CAMPO_NASCIMENTO) ?: ""
            val resultado = validador.validar(nascimentoTexto)
            return PerfilUiState(
                carregando = perfilSalvo == null,
                nome = local.nome ?: perfilSalvo?.nome ?: "",
                nascimento = nascimentoTexto,
                erroNascimento = erroExibivel(resultado),
                signo = signoExibivel(resultado),
                quantidadeDeCrencasAtivas = perfilSalvo?.crencasAtivas?.size ?: 0,
                lembreteFechamentoAtivo = notificacoes.lembreteFechamentoAtivo,
                horarioLembreteFechamento = notificacoes.horarioLembreteFechamento,
                alertaHorarioAposFechamento =
                    !notificacoes.horarioLembreteFechamento.isBefore(
                        HORARIO_FECHAMENTO_DAS_APOSTAS,
                    ),
                notificacaoResultadoAtiva = notificacoes.notificacaoResultadoAtiva,
                isPro = assinatura is EstadoDaAssinatura.Assinante,
                productIdDaAssinatura = (assinatura as? EstadoDaAssinatura.Assinante)?.productId,
            )
        }

        private fun erroExibivel(resultado: ResultadoDataNascimento): ErroDataNascimento? =
            when (resultado) {
                is ResultadoDataNascimento.Valida ->
                    if (verificador.ehMaiorDeIdade(resultado.data)) null else ErroDataNascimento.MENOR_DE_IDADE
                is ResultadoDataNascimento.Invalida ->
                    resultado.erro.takeUnless { it == ErroDataNascimento.VAZIO }
            }

        private fun signoExibivel(resultado: ResultadoDataNascimento): Signo? =
            when (resultado) {
                is ResultadoDataNascimento.Valida -> signoDe(resultado.data)
                is ResultadoDataNascimento.Invalida -> null
            }

        // RF-07.1 — nome grava a cada alteração (não tem estado inválido);
        // nascimento/signo só gravam quando a data é válida E maior de
        // idade (mesma validação do cadastro, RF-01.4/01.6) — um valor
        // inválido fica visível no campo mas nunca sobrescreve o que já
        // estava salvo.
        fun aoAlterarNome(valor: String) {
            nomeEditado.value = valor
            persistirPerfil()
        }

        fun aoAlterarNascimento(valor: String) {
            nascimentoEditado.value = formatarDataNascimento(valor)
            persistirPerfil()
        }

        private fun persistirPerfil() {
            viewModelScope.launch {
                val perfilAtual = perfil.first() ?: return@launch
                val nomeFinal = nomeEditado.value ?: perfilAtual.nome
                val nascimentoTexto =
                    nascimentoEditado.value ?: perfilAtual.nascimento?.format(FORMATO_CAMPO_NASCIMENTO) ?: ""
                val resultado = validador.validar(nascimentoTexto)
                val podeGravarNascimento =
                    resultado is ResultadoDataNascimento.Valida && erroExibivel(resultado) == null
                val (nascimentoParaSalvar, signoParaSalvar) =
                    if (podeGravarNascimento) {
                        val data = resultado.data
                        data to signoDe(data)
                    } else {
                        perfilAtual.nascimento to perfilAtual.signo
                    }
                preferenciasRepository.salvarPerfil(
                    nome = nomeFinal,
                    nascimento = nascimentoParaSalvar,
                    signo = signoParaSalvar,
                    crencasAtivas = perfilAtual.crencasAtivas,
                )
            }
        }

        // RF-07.4 — atalhos sugeridos e escolha livre chamam o mesmo método:
        // ambos produzem um LocalTime já resolvido, a origem não importa aqui.
        fun aoEscolherHorarioLembrete(horario: LocalTime) {
            viewModelScope.launch {
                val notificacoesAtuais = preferenciasDeNotificacao.first()
                val novo = notificacoesAtuais.copy(horarioLembreteFechamento = horario)
                preferenciasRepository.salvarPreferenciasDeNotificacao(novo)
                if (novo.lembreteFechamentoAtivo) scheduler.agendarLembreteFechamento(horario)
            }
        }

        fun aoAlternarLembreteFechamento(ativo: Boolean) {
            viewModelScope.launch {
                val atual = preferenciasDeNotificacao.first()
                preferenciasRepository.salvarPreferenciasDeNotificacao(atual.copy(lembreteFechamentoAtivo = ativo))
                if (ativo) {
                    scheduler.agendarLembreteFechamento(atual.horarioLembreteFechamento)
                    canalDeEventos.send(PerfilEvento.PedirPermissaoDeNotificacao)
                } else {
                    scheduler.cancelarLembreteFechamento()
                }
            }
        }

        fun aoAlternarNotificacaoResultado(ativo: Boolean) {
            viewModelScope.launch {
                val atual = preferenciasDeNotificacao.first()
                preferenciasRepository.salvarPreferenciasDeNotificacao(atual.copy(notificacaoResultadoAtiva = ativo))
                if (ativo) {
                    scheduler.agendarNotificacaoResultado()
                    canalDeEventos.send(PerfilEvento.PedirPermissaoDeNotificacao)
                } else {
                    scheduler.cancelarNotificacaoResultado()
                }
            }
        }

        // LGPD/achado de auditoria de segurança — direito de eliminação:
        // apaga perfil/crenças/notificações (DataStore) e todos os palpites
        // (Room), e cancela qualquer lembrete/notificação agendados (senão
        // ficariam órfãos, disparando depois dos dados já apagados). Nome e
        // data de nascimento são os únicos dados pessoais coletados pelo
        // Trevo (CLAUDE.md) — isso cobre os dois.
        fun aoConfirmarExclusaoDeDados() {
            viewModelScope.launch {
                scheduler.cancelarLembreteFechamento()
                scheduler.cancelarNotificacaoResultado()
                palpiteRepository.excluirTodos()
                preferenciasRepository.excluirTudo()
                canalDeEventos.send(PerfilEvento.DadosExcluidos)
            }
        }
    }
