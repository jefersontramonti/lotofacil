package com.trevo.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val NOME_DO_BANCO_DE_TESTE = "trevo-migracao-teste"

// RNF-06.6 — migrações versionadas, sem perda de dado. A versão 2 só
// acrescenta a tabela `resultados`; este teste garante que os palpites já
// salvos na versão 1 continuam intactos depois da migração.
class TrevoDatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), TrevoDatabase::class.java)

    @Test
    fun migra1Para2CriaATabelaDeResultadosSemPerderPalpitesExistentes() {
        var db = helper.createDatabase(NOME_DO_BANCO_DE_TESTE, 1)
        db.execSQL(
            "INSERT INTO palpites (dezenas, dezenasFixas, contribuicoes, forca, criadoEmEpochMillis) " +
                "VALUES ('1,2,3', '', '', 50, 1000)",
        )
        db.close()

        db = helper.runMigrationsAndValidate(NOME_DO_BANCO_DE_TESTE, 2, true, MIGRATION_1_2)

        val cursorPalpites = db.query("SELECT dezenas FROM palpites")
        assertTrue(cursorPalpites.moveToFirst())
        assertEquals("1,2,3", cursorPalpites.getString(0))
        cursorPalpites.close()

        val cursorResultados = db.query("SELECT COUNT(*) FROM resultados")
        assertTrue(cursorResultados.moveToFirst())
        assertEquals(0, cursorResultados.getInt(0))
        cursorResultados.close()

        db.close()
    }
}
