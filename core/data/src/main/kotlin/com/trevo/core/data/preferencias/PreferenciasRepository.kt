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
}
