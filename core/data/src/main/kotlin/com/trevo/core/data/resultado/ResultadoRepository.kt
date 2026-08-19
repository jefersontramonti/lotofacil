package com.trevo.core.data.resultado

import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.flow.Flow

interface ResultadoRepository {
    // RF-05.1/05.2/RNF-02.4: busca na API com recuo exponencial, cacheia
    // em Room e devolve o resultado. Lança se todas as tentativas falharem
    // — quem chama decide qual estado mostrar (sem conexão vs. falha do
    // serviço) a partir do tipo da exceção.
    suspend fun buscarUltimoResultado(): Resultado

    fun observarUltimoResultadoSalvo(): Flow<Resultado?>

    // RF-06.1 — todo resultado já buscado, mais recente primeiro. O
    // histórico casa cada um com os palpites do dia correspondente.
    fun observarTodosOsResultados(): Flow<List<Resultado>>

    // RF-05.10 — fallback manual quando a API está fora do ar por muito
    // tempo. Sem faixasDePremio: valor de prêmio não existe sem a API.
    suspend fun salvarResultadoManual(dezenas: Set<Int>)
}
