package com.trevo.core.data.preferencias

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PreferenciasRepositoryTest {
    private lateinit var repositorio: PreferenciasRepository
    private lateinit var arquivo: java.io.File

    @Before
    fun montarDataStoreTemporario() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore =
            PreferenceDataStoreFactory.create(scope = escopo) {
                context.preferencesDataStoreFile("preferencias_teste_${System.nanoTime()}")
            }
        arquivo = context.preferencesDataStoreFile("preferencias_teste").parentFile!!
        repositorio = PreferenciasRepositoryImpl(dataStore)
    }

    @After
    fun limparArquivosDeTeste() {
        arquivo.listFiles { f -> f.name.startsWith("preferencias_teste_") }?.forEach { it.delete() }
    }

    @Test
    fun semPerfilSalvoObservarPerfilDevolveNulo() =
        runTest {
            assertNull(repositorio.observarPerfil().first())
        }

    @Test
    fun perfilSalvoEDepoisObservadoVoltaComTodosOsCampos() =
        runTest {
            repositorio.salvarPerfil(
                nome = "Marlene",
                nascimento = LocalDate.of(1978, 7, 14),
                signo = Signo.CANCER,
                crencasAtivas = setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO),
            )

            val perfil = repositorio.observarPerfil().first()

            assertEquals("Marlene", perfil?.nome)
            assertEquals(LocalDate.of(1978, 7, 14), perfil?.nascimento)
            assertEquals(Signo.CANCER, perfil?.signo)
            assertEquals(setOf(Crenca.SIGNO, Crenca.LUA, Crenca.SONHO), perfil?.crencasAtivas)
        }

    @Test
    fun perfilSalvoSemNascimentoNemSignoVoltaComEssesCamposNulos() =
        runTest {
            repositorio.salvarPerfil(nome = "Marlene", nascimento = null, signo = null, crencasAtivas = emptySet())

            val perfil = repositorio.observarPerfil().first()

            assertEquals("Marlene", perfil?.nome)
            assertNull(perfil?.nascimento)
            assertNull(perfil?.signo)
        }

    @Test
    fun grupoDoSonhoConfirmadoHojeApareceParaAMesmaData() =
        runTest {
            val hoje = LocalDate.of(2026, 8, 17)

            repositorio.confirmarGrupoDoSonho(grupo = 9, hoje = hoje)

            assertEquals(9, repositorio.observarGrupoDoSonhoDeHoje(hoje).first())
        }

    @Test
    fun grupoDoSonhoConfirmadoOntemNaoApareceParaHoje() =
        runTest {
            val ontem = LocalDate.of(2026, 8, 16)
            val hoje = LocalDate.of(2026, 8, 17)

            repositorio.confirmarGrupoDoSonho(grupo = 9, hoje = ontem)

            assertNull(repositorio.observarGrupoDoSonhoDeHoje(hoje).first())
        }

    @Test
    fun semUsoNenhumRestamOPalpiteGratisDoDia() =
        runTest {
            val hoje = LocalDate.of(2026, 8, 17)

            assertEquals(1, repositorio.observarPalpitesGratisRestantesHoje(hoje).first())
        }

    @Test
    fun aoUsarOPalpiteGratisNaoSobraNenhumRestante() =
        runTest {
            val hoje = LocalDate.of(2026, 8, 17)

            repositorio.registrarPalpiteGratisUsado(hoje)

            assertEquals(0, repositorio.observarPalpitesGratisRestantesHoje(hoje).first())
        }

    @Test
    fun anuncioAssistidoCreditaMaisUmPalpiteNoDia() =
        runTest {
            val hoje = LocalDate.of(2026, 8, 17)

            repositorio.registrarPalpiteGratisUsado(hoje)
            repositorio.registrarAnuncioAssistido(hoje)

            assertEquals(1, repositorio.observarPalpitesGratisRestantesHoje(hoje).first())
        }

    @Test
    fun usoRegistradoOntemNaoAfetaOLimiteDeHoje() =
        runTest {
            val ontem = LocalDate.of(2026, 8, 16)
            val hoje = LocalDate.of(2026, 8, 17)

            repositorio.registrarPalpiteGratisUsado(ontem)

            assertEquals(1, repositorio.observarPalpitesGratisRestantesHoje(hoje).first())
        }
}
