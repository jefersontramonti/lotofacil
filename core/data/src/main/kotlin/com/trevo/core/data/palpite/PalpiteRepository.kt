package com.trevo.core.data.palpite

import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface PalpiteRepository {
    suspend fun salvar(palpite: Palpite): Long

    suspend fun excluir(id: Long)

    fun observarPalpitesDoDia(
        dia: LocalDate,
        zona: ZoneId,
    ): Flow<List<PalpiteSalvo>>

    fun observarPalpitePorId(id: Long): Flow<PalpiteSalvo?>

    // `criadoEm` vem de quem chama (já tem o PalpiteSalvo carregado) — o
    // repositório não faz uma leitura extra só pra preservar o horário
    // original do palpite editado/refeito.
    suspend fun atualizar(
        id: Long,
        palpite: Palpite,
        criadoEm: Instant,
    )
}
