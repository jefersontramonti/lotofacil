package com.trevo.core.data.preferencias

import java.time.LocalTime

val HORARIO_LEMBRETE_PADRAO: LocalTime = LocalTime.of(18, 0)

// RF-07.7: os dois avisos começam desligados — a permissão do sistema só é
// pedida quando o usuário liga o primeiro (nunca na abertura do app), e não
// existe estado "ligado sem permissão pedida" possível se o padrão é
// desligado.
data class PreferenciasDeNotificacao(
    val lembreteFechamentoAtivo: Boolean = false,
    val horarioLembreteFechamento: LocalTime = HORARIO_LEMBRETE_PADRAO,
    val notificacaoResultadoAtiva: Boolean = false,
)
