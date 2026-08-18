package com.trevo.app.resultado

import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import java.time.LocalDate

class FakeResultadoRepository(
    private val clock: Clock,
) : ResultadoRepository {
    var proximaExcecao: Throwable? = null
    var proximoResultado: Resultado? = null

    private val ultimoSalvo = MutableStateFlow<Resultado?>(null)
    val salvo: StateFlow<Resultado?> = ultimoSalvo.asStateFlow()

    override suspend fun buscarUltimoResultado(): Resultado {
        proximaExcecao?.let { throw it }
        val resultado = checkNotNull(proximoResultado) { "sem resultado configurado no fake" }
        ultimoSalvo.value = resultado
        return resultado
    }

    override fun observarUltimoResultadoSalvo(): StateFlow<Resultado?> = salvo

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
        proximoResultado = resultado
        proximaExcecao = null
        ultimoSalvo.value = resultado
    }
}
