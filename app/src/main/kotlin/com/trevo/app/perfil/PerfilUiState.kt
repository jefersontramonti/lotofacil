package com.trevo.app.perfil

import com.trevo.core.data.preferencias.HORARIO_LEMBRETE_PADRAO
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo
import java.time.LocalTime

data class PerfilUiState(
    val carregando: Boolean = true,
    val nome: String = "",
    val nascimento: String = "",
    val erroNascimento: ErroDataNascimento? = null,
    val signo: Signo? = null,
    val quantidadeDeCrencasAtivas: Int = 0,
    val lembreteFechamentoAtivo: Boolean = false,
    val horarioLembreteFechamento: LocalTime = HORARIO_LEMBRETE_PADRAO,
    // RF-07.5
    val alertaHorarioAposFechamento: Boolean = false,
    val notificacaoResultadoAtiva: Boolean = false,
)
