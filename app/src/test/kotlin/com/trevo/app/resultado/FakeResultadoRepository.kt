package com.trevo.app.resultado

import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

class FakeResultadoRepository(
    private val clock: Clock,
) : ResultadoRepository {
    var proximaExcecao: Throwable? = null
    var proximoResultado: Resultado? = null

    // Mesma semântica do ResultadoDao real: só acumula, nunca substitui —
    // índice 0 é o mais recente.
    private val todos = MutableStateFlow<List<Resultado>>(emptyList())

    fun adicionarResultado(resultado: Resultado) {
        todos.value = listOf(resultado) + todos.value
    }

    override suspend fun buscarUltimoResultado(): Resultado {
        proximaExcecao?.let { throw it }
        val resultado = checkNotNull(proximoResultado) { "sem resultado configurado no fake" }
        adicionarResultado(resultado)
        return resultado
    }

    override fun observarUltimoResultadoSalvo(): Flow<Resultado?> = todos.map { it.firstOrNull() }

    override fun observarTodosOsResultados(): Flow<List<Resultado>> = todos

    override suspend fun salvarResultadoManual(dezenas: Set<Int>) {
        val resultado =
            Resultado(
                numero = null,
                dataApuracao = LocalDate.now(clock),
                dezenasSorteadas = dezenas.sorted(),
                faixasDePremio = emptyList(),
                acumulado = false,
                origem = OrigemDoResultado.MANUAL,
                proximoConcurso = null,
            )
        proximoResultado = resultado
        proximaExcecao = null
        adicionarResultado(resultado)
    }
}
