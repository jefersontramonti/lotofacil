package com.trevo.app.preferencias

import com.trevo.core.data.preferencias.PerfilSalvo
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakePreferenciasRepository : PreferenciasRepository {
    private val perfil = MutableStateFlow<PerfilSalvo?>(null)
    val perfilSalvo: StateFlow<PerfilSalvo?> = perfil.asStateFlow()

    private data class SonhoConfirmado(
        val grupo: Int,
        val data: LocalDate,
    )

    private val sonhoDoDia = MutableStateFlow<SonhoConfirmado?>(null)

    override suspend fun salvarPerfil(
        nome: String,
        nascimento: LocalDate?,
        signo: Signo?,
        crencasAtivas: Set<Crenca>,
    ) {
        perfil.value = PerfilSalvo(nome = nome, nascimento = nascimento, signo = signo, crencasAtivas = crencasAtivas)
    }

    override fun observarPerfil() = perfil.asStateFlow()

    override suspend fun confirmarGrupoDoSonho(
        grupo: Int,
        hoje: LocalDate,
    ) {
        sonhoDoDia.value = SonhoConfirmado(grupo, hoje)
    }

    override fun observarGrupoDoSonhoDeHoje(hoje: LocalDate) =
        sonhoDoDia.map { confirmado -> confirmado?.takeIf { it.data == hoje }?.grupo }
}
