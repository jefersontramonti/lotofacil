package com.trevo.core.data.palpite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PalpiteDao {
    @Insert
    suspend fun inserir(palpite: PalpiteEntity): Long

    @Update
    suspend fun atualizar(palpite: PalpiteEntity)

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

    @Query("SELECT * FROM palpites WHERE id = :id")
    fun observarPorId(id: Long): Flow<PalpiteEntity?>

    // RF-06 — histórico precisa de todos os palpites já salvos, não só os
    // de um dia, pra casar cada um com o resultado do seu concurso.
    @Query("SELECT * FROM palpites ORDER BY criadoEmEpochMillis DESC")
    fun observarTodos(): Flow<List<PalpiteEntity>>
}
