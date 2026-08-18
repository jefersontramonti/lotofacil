package com.trevo.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trevo.core.data.palpite.PalpiteDao
import com.trevo.core.data.palpite.PalpiteEntity
import com.trevo.core.data.resultado.ResultadoDao
import com.trevo.core.data.resultado.ResultadoEntity

@Database(entities = [PalpiteEntity::class, ResultadoEntity::class], version = 2)
abstract class TrevoDatabase : RoomDatabase() {
    abstract fun palpiteDao(): PalpiteDao

    abstract fun resultadoDao(): ResultadoDao
}

// RF-05.2, nova tabela — RNF-06.6 proíbe fallbackToDestructiveMigration,
// então a migração é explícita mesmo só adicionando uma tabela.
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `resultados` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`numero` INTEGER, " +
                    "`dataApuracaoIso` TEXT NOT NULL, " +
                    "`dezenasSorteadas` TEXT NOT NULL, " +
                    "`faixas` TEXT NOT NULL, " +
                    "`acumulado` INTEGER NOT NULL, " +
                    "`origem` TEXT NOT NULL)",
            )
        }
    }
