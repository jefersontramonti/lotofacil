package com.trevo.core.data.notificacoes

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.trevo.core.engine.notificacao.calcularAtrasoAteProximoHorario
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val TRABALHO_LEMBRETE_FECHAMENTO = "lembrete_fechamento"
const val TRABALHO_NOTIFICACAO_RESULTADO = "notificacao_resultado"

// RF-07.6 — horário fixo, não configurável pelo usuário (só o lembrete de
// RF-07.4 tem seletor de horário). O wireframe 1m diz "20h", mas o sorteio
// só sai às 21h (Docs/tabelavalores.md, mesma correção de RF-03.1/
// home_horario_apostas) — checar às 20h reavisaria sobre o resultado de
// ontem sem nada novo pra mostrar. 21h30 dá margem pra Caixa publicar.
val HORARIO_NOTIFICACAO_RESULTADO: LocalTime = LocalTime.of(21, 30)

interface NotificacoesScheduler {
    fun agendarLembreteFechamento(horario: LocalTime)

    fun cancelarLembreteFechamento()

    fun agendarNotificacaoResultado()

    fun cancelarNotificacaoResultado()
}

// Agendamento por WorkManager (nunca AlarmManager exato): um lembrete não
// precisa disparar no minuto exato, e evitar SCHEDULE_EXACT_ALARM mantém o
// app fora da restrição de alarme exato do Android 12+. Cada worker
// reagenda a si mesmo para o dia seguinte ao terminar (ver
// LembreteFechamentoWorker/ResultadoSorteioWorker) — esta classe só cuida do
// primeiro agendamento e do cancelamento imediato ao desligar o toggle.
class NotificacoesSchedulerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clock: Clock,
    ) : NotificacoesScheduler {
        override fun agendarLembreteFechamento(horario: LocalTime) {
            val atraso = calcularAtrasoAteProximoHorario(LocalDateTime.now(clock), horario)
            val pedido =
                OneTimeWorkRequestBuilder<LembreteFechamentoWorker>()
                    .setInitialDelay(atraso.toMillis(), TimeUnit.MILLISECONDS)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(TRABALHO_LEMBRETE_FECHAMENTO, ExistingWorkPolicy.REPLACE, pedido)
        }

        override fun cancelarLembreteFechamento() {
            WorkManager.getInstance(context).cancelUniqueWork(TRABALHO_LEMBRETE_FECHAMENTO)
        }

        override fun agendarNotificacaoResultado() {
            val atraso = calcularAtrasoAteProximoHorario(LocalDateTime.now(clock), HORARIO_NOTIFICACAO_RESULTADO)
            val pedido =
                OneTimeWorkRequestBuilder<ResultadoSorteioWorker>()
                    .setInitialDelay(atraso.toMillis(), TimeUnit.MILLISECONDS)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(TRABALHO_NOTIFICACAO_RESULTADO, ExistingWorkPolicy.REPLACE, pedido)
        }

        override fun cancelarNotificacaoResultado() {
            WorkManager.getInstance(context).cancelUniqueWork(TRABALHO_NOTIFICACAO_RESULTADO)
        }
    }
