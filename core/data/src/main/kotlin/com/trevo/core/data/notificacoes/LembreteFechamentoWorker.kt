package com.trevo.core.data.notificacoes

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trevo.core.data.R
import com.trevo.core.data.preferencias.PreferenciasRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

// RF-07.3 — dispara o lembrete se ainda estiver ligado (pode ter sido
// desligado entre o agendamento e a execução) e sempre reagenda para o
// próximo dia — é o worker, não o toggle, quem mantém a cadeia diária viva.
@HiltWorker
class LembreteFechamentoWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val preferenciasRepository: PreferenciasRepository,
        private val scheduler: NotificacoesScheduler,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val preferencias = preferenciasRepository.observarPreferenciasDeNotificacao().first()
            if (preferencias.lembreteFechamentoAtivo) {
                mostrarNotificacao()
                scheduler.agendarLembreteFechamento(preferencias.horarioLembreteFechamento)
            }
            return Result.success()
        }

        private fun mostrarNotificacao() {
            // Cobre tanto o toggle de sistema (Android 13+) quanto a
            // permissão de notificação negada — nunca chama notify() sem
            // essa checagem (SecurityException em vez de silêncio).
            if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return

            val notificacao =
                NotificationCompat
                    .Builder(applicationContext, CANAL_LEMBRETE_FECHAMENTO)
                    .setSmallIcon(R.drawable.ic_notificacao)
                    .setContentTitle(applicationContext.getString(R.string.notificacao_lembrete_titulo))
                    .setContentText(applicationContext.getString(R.string.notificacao_lembrete_texto))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntentParaAbrirApp(applicationContext, ID_NOTIFICACAO_LEMBRETE_FECHAMENTO))
                    .build()

            NotificationManagerCompat.from(applicationContext).notify(ID_NOTIFICACAO_LEMBRETE_FECHAMENTO, notificacao)
        }
    }

internal fun pendingIntentParaAbrirApp(
    context: Context,
    idPedido: Int,
): PendingIntent? {
    val intencao =
        context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            ?: return null
    return PendingIntent.getActivity(
        context,
        idPedido,
        intencao,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
