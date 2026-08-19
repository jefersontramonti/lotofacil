package com.trevo.core.data.resultado

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultadoDao {
    @Insert
    suspend fun inserir(resultado: ResultadoEntity): Long

    @Query("SELECT * FROM resultados ORDER BY id DESC LIMIT 1")
    fun observarUltimo(): Flow<ResultadoEntity?>

    // RF-06 — o histórico reaproveita todo resultado já buscado (a tabela só
    // acumula, nunca substitui), não uma fonte separada.
    @Query("SELECT * FROM resultados ORDER BY id DESC")
    fun observarTodos(): Flow<List<ResultadoEntity>>
}
