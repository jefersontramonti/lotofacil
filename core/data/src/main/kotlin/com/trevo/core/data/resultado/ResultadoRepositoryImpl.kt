package com.trevo.core.data.resultado

import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class ResultadoRepositoryImpl
    @Inject
    constructor(
        private val api: ResultadoApi,
        private val dao: ResultadoDao,
        private val clock: Clock,
    ) : ResultadoRepository {
        override suspend fun buscarUltimoResultado(): Resultado {
            val resultado = comRecuoExponencial { api.buscarUltimo() }.paraDominio()
            dao.inserir(resultado.paraEntity())
            return resultado
        }

        override fun observarUltimoResultadoSalvo(): Flow<Resultado?> =
            dao.observarUltimo().map { entidade -> entidade?.paraDominio() }

        override suspend fun salvarResultadoManual(dezenas: Set<Int>) {
            val resultado =
                Resultado(
                    numero = null,
                    dataApuracao = LocalDate.now(clock),
                    dezenasSorteadas = dezenas.sorted(),
                    faixasDePremio = emptyList(),
                    acumulado = false,
                    origem = OrigemDoResultado.MANUAL,
                )
            dao.inserir(resultado.paraEntity())
        }
    }
