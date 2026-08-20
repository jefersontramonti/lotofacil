package com.trevo.core.data.notificacoes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.trevo.core.data.R

const val CANAL_LEMBRETE_FECHAMENTO = "lembrete_fechamento"
const val CANAL_RESULTADO_SORTEIO = "resultado_sorteio"

const val ID_NOTIFICACAO_LEMBRETE_FECHAMENTO = 1001
const val ID_NOTIFICACAO_RESULTADO_SORTEIO = 1002

// minSdk 26 é o próprio Android O que introduziu NotificationChannel — sem
// checagem de versão, sempre disponível. Chamado uma vez em
// TrevoApplication.onCreate(); criar um canal já existente com os mesmos
// parâmetros é inofensivo (a API substitui em vez de duplicar).
fun criarCanaisDeNotificacao(context: Context) {
    val gerenciador = context.getSystemService(NotificationManager::class.java)
    gerenciador.createNotificationChannel(
        NotificationChannel(
            CANAL_LEMBRETE_FECHAMENTO,
            context.getString(R.string.notificacao_canal_lembrete_nome),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notificacao_canal_lembrete_descricao)
        },
    )
    gerenciador.createNotificationChannel(
        NotificationChannel(
            CANAL_RESULTADO_SORTEIO,
            context.getString(R.string.notificacao_canal_resultado_nome),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notificacao_canal_resultado_descricao)
        },
    )
}
