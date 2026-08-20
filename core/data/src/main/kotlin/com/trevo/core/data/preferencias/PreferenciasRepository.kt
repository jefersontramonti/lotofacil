package com.trevo.core.data.preferencias

import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PreferenciasRepository {
    suspend fun salvarPerfil(
        nome: String,
        nascimento: LocalDate?,
        signo: Signo?,
        crencasAtivas: Set<Crenca>,
    )

    fun observarPerfil(): Flow<PerfilSalvo?>

    suspend fun confirmarGrupoDoSonho(
        grupo: Int,
        hoje: LocalDate,
    )

    fun observarGrupoDoSonhoDeHoje(hoje: LocalDate): Flow<Int?>

    suspend fun salvarPreferenciasDeNotificacao(preferencias: PreferenciasDeNotificacao)

    fun observarPreferenciasDeNotificacao(): Flow<PreferenciasDeNotificacao>

    // RF-09.1: 1 palpite grátis por dia, reinício à meia-noite no fuso do
    // aparelho — mesmo padrão de confirmarGrupoDoSonho/observarGrupoDoSonhoDeHoje,
    // a data salva vs. `hoje` decide o reset, nunca um job/alarme separado.
    suspend fun registrarPalpiteGratisUsado(hoje: LocalDate)

    // RF-09.2: anúncio recompensado credita mais um palpite no dia.
    suspend fun registrarAnuncioAssistido(hoje: LocalDate)

    fun observarPalpitesGratisRestantesHoje(hoje: LocalDate): Flow<Int>
}
