package com.trevo.core.data.palpite

import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

interface PalpiteRepository {
    suspend fun salvar(palpite: Palpite): Long

    suspend fun excluir(id: Long)

    fun observarPalpitesDoDia(
        dia: LocalDate,
        zona: ZoneId,
    ): Flow<List<PalpiteSalvo>>
}
