package com.trevo.app.palpite

import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteSalvo
import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FakePalpiteRepository(
    private val clock: Clock,
) : PalpiteRepository {
    private var proximoId = 1L
    private val estado = MutableStateFlow<List<PalpiteSalvo>>(emptyList())
    val todos: StateFlow<List<PalpiteSalvo>> = estado.asStateFlow()

    override suspend fun salvar(palpite: Palpite): Long {
        val id = proximoId++
        // Instant.now(clock) sozinho repete o mesmo instante em relógio fixo
        // (comum nos testes) — o deslocamento por id garante ordem de
        // criação estável, igual ao autoincremento + timestamp real do Room.
        val criadoEm = Instant.now(clock).plusMillis(id)
        estado.value = estado.value + PalpiteSalvo(id = id, palpite = palpite, criadoEm = criadoEm)
        return id
    }

    override suspend fun excluir(id: Long) {
        estado.value = estado.value.filterNot { it.id == id }
    }

    override fun observarPalpitesDoDia(
        dia: LocalDate,
        zona: ZoneId,
    ) = estado.map { salvos ->
        salvos.filter { it.criadoEm.atZone(zona).toLocalDate() == dia }.sortedByDescending { it.criadoEm }
    }

    override fun observarPalpitePorId(id: Long) = estado.map { salvos -> salvos.firstOrNull { it.id == id } }

    override suspend fun atualizar(
        id: Long,
        palpite: Palpite,
        criadoEm: Instant,
    ) {
        estado.value = estado.value.map { if (it.id == id) PalpiteSalvo(id, palpite, criadoEm) else it }
    }
}
