package com.trevo.core.data.palpite

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.core.data.TrevoDatabase
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class PalpiteRepositoryTest {
    private val zona = ZoneOffset.UTC
    private val agora = Instant.parse("2026-08-17T12:00:00Z")

    private lateinit var banco: TrevoDatabase
    private lateinit var repositorio: PalpiteRepository

    @Before
    fun montarBancoEmMemoria() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        banco = Room.inMemoryDatabaseBuilder(context, TrevoDatabase::class.java).allowMainThreadQueries().build()
        repositorio = PalpiteRepositoryImpl(dao = banco.palpiteDao(), clock = Clock.fixed(agora, zona))
    }

    @After
    fun fecharBanco() {
        banco.close()
    }

    private val palpiteDeExemplo =
        Palpite(
            dezenas = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            dezenasFixas = listOf(1, 2),
            contribuicoes = mapOf(Crenca.SIGNO to listOf(1, 2, 3), Crenca.LUA to listOf(4, 5)),
            forca = 80,
        )

    @Test
    fun salvarEDepoisObservarDevolveOMesmoPalpiteComTodosOsCampos() =
        runTest {
            repositorio.salvar(palpiteDeExemplo)

            val salvos = repositorio.observarPalpitesDoDia(LocalDate.of(2026, 8, 17), zona).first()

            assertEquals(1, salvos.size)
            assertEquals(palpiteDeExemplo, salvos.first().palpite)
        }

    @Test
    fun observarPalpitesDoDiaIgnoraPalpitesDeOutrosDias() =
        runTest {
            repositorio.salvar(palpiteDeExemplo)

            val diaSeguinte = repositorio.observarPalpitesDoDia(LocalDate.of(2026, 8, 18), zona).first()
            val diaAnterior = repositorio.observarPalpitesDoDia(LocalDate.of(2026, 8, 16), zona).first()

            assertTrue(diaSeguinte.isEmpty())
            assertTrue(diaAnterior.isEmpty())
        }

    @Test
    fun excluirRemoveOPalpiteDaListagemDoDia() =
        runTest {
            val id = repositorio.salvar(palpiteDeExemplo)

            repositorio.excluir(id)

            val salvos = repositorio.observarPalpitesDoDia(LocalDate.of(2026, 8, 17), zona).first()
            assertTrue(salvos.isEmpty())
        }
}
