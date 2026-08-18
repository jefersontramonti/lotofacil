package com.trevo.core.data.resultado

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

private const val TENTATIVAS_MAXIMAS = 5
private const val ATRASO_INICIAL_MS = 500L

// RNF-02.4 — recuo exponencial, no máximo 5 tentativas por concurso. Só
// falha de rede (IOException) e erro HTTP (HttpException) valem nova
// tentativa; qualquer outra exceção (serialização, cancelamento de
// coroutine) propaga na hora, sem retry inútil.
suspend fun <T> comRecuoExponencial(
    tentativasMaximas: Int = TENTATIVAS_MAXIMAS,
    block: suspend () -> T,
): T {
    var atraso = ATRASO_INICIAL_MS
    repeat(tentativasMaximas - 1) {
        try {
            return block()
        } catch (e: IOException) {
            delay(atraso)
            atraso *= 2
        } catch (e: HttpException) {
            delay(atraso)
            atraso *= 2
        }
    }
    return block()
}
