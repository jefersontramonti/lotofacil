package com.trevo.core.data.palpite

import com.trevo.core.engine.palpite.Palpite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class PalpiteRepositoryImpl
    @Inject
    constructor(
        private val dao: PalpiteDao,
        private val clock: Clock,
    ) : PalpiteRepository {
        override suspend fun salvar(palpite: Palpite): Long =
            dao.inserir(palpite.paraEntity(criadoEm = Instant.now(clock)))

        override suspend fun excluir(id: Long) = dao.excluirPorId(id)

        override fun observarPalpitesDoDia(
            dia: LocalDate,
            zona: ZoneId,
        ): Flow<List<PalpiteSalvo>> {
            val inicioDoDia = dia.atStartOfDay(zona).toInstant().toEpochMilli()
            val fimDoDia =
                dia
                    .plusDays(1)
                    .atStartOfDay(zona)
                    .toInstant()
                    .toEpochMilli() - 1
            return dao.observarEntre(inicioDoDia, fimDoDia).map { entidades -> entidades.map { it.paraDominio() } }
        }
    }
