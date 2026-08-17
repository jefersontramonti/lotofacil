package com.trevo.core.data.palpite

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.core.data.TrevoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PalpiteDaoTest {
    private lateinit var banco: TrevoDatabase
    private lateinit var dao: PalpiteDao

    @Before
    fun montarBancoEmMemoria() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        banco = Room.inMemoryDatabaseBuilder(context, TrevoDatabase::class.java).allowMainThreadQueries().build()
        dao = banco.palpiteDao()
    }

    @After
    fun fecharBanco() {
        banco.close()
    }

    private fun entidade(
        criadoEmEpochMillis: Long,
        dezenas: String = "1,2,3",
    ) = PalpiteEntity(
        dezenas = dezenas,
        dezenasFixas = "",
        contribuicoes = "",
        forca = 50,
        criadoEmEpochMillis = criadoEmEpochMillis,
    )

    @Test
    fun observarEntreDevolveApenasOsPalpitesDentroDoIntervaloOrdenadosDoMaisRecenteAoMaisAntigo() =
        runTest {
            dao.inserir(entidade(criadoEmEpochMillis = 1_000, dezenas = "1"))
            dao.inserir(entidade(criadoEmEpochMillis = 2_000, dezenas = "2"))
            dao.inserir(entidade(criadoEmEpochMillis = 3_000, dezenas = "3"))

            val resultado = dao.observarEntre(inicioEpochMillis = 1_500, fimEpochMillis = 3_000).first()

            assertEquals(listOf("3", "2"), resultado.map { it.dezenas })
        }

    @Test
    fun excluirPorIdRemoveApenasOPalpiteCorrespondente() =
        runTest {
            val idParaExcluir = dao.inserir(entidade(criadoEmEpochMillis = 1_000, dezenas = "1"))
            dao.inserir(entidade(criadoEmEpochMillis = 2_000, dezenas = "2"))

            dao.excluirPorId(idParaExcluir)

            val restantes = dao.observarEntre(inicioEpochMillis = 0, fimEpochMillis = Long.MAX_VALUE).first()
            assertEquals(listOf("2"), restantes.map { it.dezenas })
        }
}
