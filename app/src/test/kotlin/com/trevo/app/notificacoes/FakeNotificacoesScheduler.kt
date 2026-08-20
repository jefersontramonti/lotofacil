package com.trevo.app.notificacoes

import com.trevo.core.data.notificacoes.NotificacoesScheduler
import java.time.LocalTime

class FakeNotificacoesScheduler : NotificacoesScheduler {
    var horarioLembreteAgendado: LocalTime? = null
        private set
    var lembreteCancelado: Boolean = false
        private set
    var notificacaoResultadoAgendada: Boolean = false
        private set
    var notificacaoResultadoCancelada: Boolean = false
        private set

    override fun agendarLembreteFechamento(horario: LocalTime) {
        horarioLembreteAgendado = horario
        lembreteCancelado = false
    }

    override fun cancelarLembreteFechamento() {
        lembreteCancelado = true
        horarioLembreteAgendado = null
    }

    override fun agendarNotificacaoResultado() {
        notificacaoResultadoAgendada = true
        notificacaoResultadoCancelada = false
    }

    override fun cancelarNotificacaoResultado() {
        notificacaoResultadoCancelada = true
        notificacaoResultadoAgendada = false
    }
}
