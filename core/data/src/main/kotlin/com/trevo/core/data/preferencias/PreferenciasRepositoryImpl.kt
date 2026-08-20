package com.trevo.core.data.preferencias

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

private val CHAVE_NOME = stringPreferencesKey("nome")
private val CHAVE_NASCIMENTO = stringPreferencesKey("nascimento_iso")
private val CHAVE_SIGNO = stringPreferencesKey("signo")
private val CHAVE_CRENCAS_ATIVAS = stringPreferencesKey("crencas_ativas")
private val CHAVE_GRUPO_DO_SONHO_NUMERO = intPreferencesKey("grupo_do_sonho_numero")
private val CHAVE_GRUPO_DO_SONHO_DATA = stringPreferencesKey("grupo_do_sonho_data_iso")
private const val SEPARADOR_CRENCAS = ","

private val CHAVE_LEMBRETE_FECHAMENTO_ATIVO = booleanPreferencesKey("lembrete_fechamento_ativo")
private val CHAVE_HORARIO_LEMBRETE_FECHAMENTO = stringPreferencesKey("horario_lembrete_fechamento")
private val CHAVE_NOTIFICACAO_RESULTADO_ATIVA = booleanPreferencesKey("notificacao_resultado_ativa")

class PreferenciasRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : PreferenciasRepository {
        override suspend fun salvarPerfil(
            nome: String,
            nascimento: LocalDate?,
            signo: Signo?,
            crencasAtivas: Set<Crenca>,
        ) {
            dataStore.edit { preferencias ->
                preferencias[CHAVE_NOME] = nome
                if (nascimento != null) {
                    preferencias[CHAVE_NASCIMENTO] = nascimento.toString()
                } else {
                    preferencias.remove(CHAVE_NASCIMENTO)
                }
                if (signo != null) {
                    preferencias[CHAVE_SIGNO] = signo.name
                } else {
                    preferencias.remove(CHAVE_SIGNO)
                }
                preferencias[CHAVE_CRENCAS_ATIVAS] = crencasAtivas.joinToString(SEPARADOR_CRENCAS) { it.name }
            }
        }

        override fun observarPerfil(): Flow<PerfilSalvo?> =
            dataStore.data.map { preferencias ->
                val nome = preferencias[CHAVE_NOME] ?: return@map null
                PerfilSalvo(
                    nome = nome,
                    nascimento = preferencias[CHAVE_NASCIMENTO]?.let { LocalDate.parse(it) },
                    signo = preferencias[CHAVE_SIGNO]?.let { Signo.valueOf(it) },
                    crencasAtivas =
                        preferencias[CHAVE_CRENCAS_ATIVAS]
                            ?.split(SEPARADOR_CRENCAS)
                            ?.filter { it.isNotEmpty() }
                            ?.map { Crenca.valueOf(it) }
                            ?.toSet()
                            ?: emptySet(),
                )
            }

        override suspend fun confirmarGrupoDoSonho(
            grupo: Int,
            hoje: LocalDate,
        ) {
            dataStore.edit { preferencias ->
                preferencias[CHAVE_GRUPO_DO_SONHO_NUMERO] = grupo
                preferencias[CHAVE_GRUPO_DO_SONHO_DATA] = hoje.toString()
            }
        }

        override fun observarGrupoDoSonhoDeHoje(hoje: LocalDate): Flow<Int?> =
            dataStore.data.map { preferencias ->
                val dataSalva = preferencias[CHAVE_GRUPO_DO_SONHO_DATA]
                if (dataSalva == hoje.toString()) preferencias[CHAVE_GRUPO_DO_SONHO_NUMERO] else null
            }

        override suspend fun salvarPreferenciasDeNotificacao(preferencias: PreferenciasDeNotificacao) {
            dataStore.edit { armazenadas ->
                armazenadas[CHAVE_LEMBRETE_FECHAMENTO_ATIVO] = preferencias.lembreteFechamentoAtivo
                armazenadas[CHAVE_HORARIO_LEMBRETE_FECHAMENTO] = preferencias.horarioLembreteFechamento.toString()
                armazenadas[CHAVE_NOTIFICACAO_RESULTADO_ATIVA] = preferencias.notificacaoResultadoAtiva
            }
        }

        override fun observarPreferenciasDeNotificacao(): Flow<PreferenciasDeNotificacao> =
            dataStore.data.map { armazenadas ->
                PreferenciasDeNotificacao(
                    lembreteFechamentoAtivo = armazenadas[CHAVE_LEMBRETE_FECHAMENTO_ATIVO] ?: false,
                    horarioLembreteFechamento =
                        armazenadas[CHAVE_HORARIO_LEMBRETE_FECHAMENTO]?.let { LocalTime.parse(it) }
                            ?: HORARIO_LEMBRETE_PADRAO,
                    notificacaoResultadoAtiva = armazenadas[CHAVE_NOTIFICACAO_RESULTADO_ATIVA] ?: false,
                )
            }
    }
