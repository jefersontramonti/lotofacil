package com.trevo.core.data.palpite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PalpiteDao {
    @Insert
    suspend fun inserir(palpite: PalpiteEntity): Long

    @Query("DELETE FROM palpites WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    @Query(
        "SELECT * FROM palpites WHERE criadoEmEpochMillis BETWEEN :inicioEpochMillis AND :fimEpochMillis " +
            "ORDER BY criadoEmEpochMillis DESC",
    )
    fun observarEntre(
        inicioEpochMillis: Long,
        fimEpochMillis: Long,
    ): Flow<List<PalpiteEntity>>
}
