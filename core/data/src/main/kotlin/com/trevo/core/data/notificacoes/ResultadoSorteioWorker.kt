package com.trevo.core.data.notificacoes

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trevo.core.data.R
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.resultado.Resultado
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

// RF-07.6 — tenta buscar o resultado às 20h; só notifica se o resultado
// buscado for o de hoje (CLAUDE.md §8: nunca inventar/supor um resultado —
// se a Caixa ainda não publicou, buscarUltimoResultado() só devolve o
// último já cacheado, de um dia anterior, e este worker fica em silêncio em
// vez de reavisar sobre um concurso já visto). Sempre reagenda para o
// próximo dia, com sucesso ou falha — uma falha de rede aqui é a mesma
// classe de instabilidade que RF-05/RNF-02.4 já esperam da API da Caixa.
@HiltWorker
class ResultadoSorteioWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val preferenciasRepository: PreferenciasRepository,
        private val resultadoRepository: ResultadoRepository,
        private val scheduler: NotificacoesScheduler,
        private val clock: Clock,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val preferencias = preferenciasRepository.observarPreferenciasDeNotificacao().first()
            if (preferencias.notificacaoResultadoAtiva) {
                val resultado =
                    try {
                        resultadoRepository.buscarUltimoResultado()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                if (resultado != null && resultado.dataApuracao == LocalDate.now(clock)) {
                    mostrarNotificacao(resultado)
                }
                scheduler.agendarNotificacaoResultado()
            }
            return Result.success()
        }

        private fun mostrarNotificacao(resultado: Resultado) {
            if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return

            val notificacao =
                NotificationCompat
                    .Builder(applicationContext, CANAL_RESULTADO_SORTEIO)
                    .setSmallIcon(R.drawable.ic_notificacao)
                    .setContentTitle(applicationContext.getString(R.string.notificacao_resultado_titulo))
                    .setContentText(applicationContext.getString(R.string.notificacao_resultado_texto))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntentParaAbrirApp(applicationContext, ID_NOTIFICACAO_RESULTADO_SORTEIO))
                    .build()

            NotificationManagerCompat.from(applicationContext).notify(ID_NOTIFICACAO_RESULTADO_SORTEIO, notificacao)
        }
    }
