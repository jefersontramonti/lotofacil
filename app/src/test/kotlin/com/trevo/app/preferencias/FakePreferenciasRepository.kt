package com.trevo.app.preferencias

import com.trevo.core.data.preferencias.PerfilSalvo
import com.trevo.core.data.preferencias.PreferenciasDeNotificacao
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

    private val preferenciasDeNotificacao = MutableStateFlow(PreferenciasDeNotificacao())

    private data class SonhoConfirmado(
        val grupo: Int,
        val data: LocalDate,
    )

    private val sonhoDoDia = MutableStateFlow<SonhoConfirmado?>(null)

    private data class LimiteDiario(
        val data: LocalDate,
        val usados: Int,
        val extras: Int,
    )

    // Espelha LIMITE_GRATIS_POR_DIA de PreferenciasRepositoryImpl (1
    // palpite grátis/dia, RF-09.1).
    private val limiteDiario = MutableStateFlow<LimiteDiario?>(null)

    // Espelha LIMITE_ANUNCIOS_POR_DIA de PreferenciasRepositoryImpl (2
    // anúncios recompensados/dia, RF-09.2).
    private val limiteAnunciosPorDia = 2

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

    override suspend fun salvarPreferenciasDeNotificacao(preferencias: PreferenciasDeNotificacao) {
        preferenciasDeNotificacao.value = preferencias
    }

    override fun observarPreferenciasDeNotificacao(): StateFlow<PreferenciasDeNotificacao> =
        preferenciasDeNotificacao.asStateFlow()

    override suspend fun registrarPalpiteGratisUsado(hoje: LocalDate) {
        val atual = limiteDiario.value?.takeIf { it.data == hoje } ?: LimiteDiario(hoje, usados = 0, extras = 0)
        limiteDiario.value = atual.copy(usados = atual.usados + 1)
    }

    override suspend fun registrarAnuncioAssistido(hoje: LocalDate) {
        val atual = limiteDiario.value?.takeIf { it.data == hoje } ?: LimiteDiario(hoje, usados = 0, extras = 0)
        if (atual.extras < limiteAnunciosPorDia) {
            limiteDiario.value = atual.copy(extras = atual.extras + 1)
        }
    }

    override fun observarPalpitesGratisRestantesHoje(hoje: LocalDate) =
        limiteDiario.map { estado ->
            val atual = estado?.takeIf { it.data == hoje }
            (1 + (atual?.extras ?: 0) - (atual?.usados ?: 0)).coerceAtLeast(0)
        }

    override fun observarAnunciosDisponiveisHoje(hoje: LocalDate) =
        limiteDiario.map { estado ->
            val extras = estado?.takeIf { it.data == hoje }?.extras ?: 0
            (limiteAnunciosPorDia - extras).coerceAtLeast(0)
        }
}
