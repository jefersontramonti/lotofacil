package com.trevo.core.data.resultado

import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// RNF-02.4 — recuo exponencial, no máximo 5 tentativas.
class RecuoExponencialTest {
    private fun httpExceptionDeExemplo() = HttpException(Response.error<Any>(500, "erro".toResponseBody(null)))

    @Test
    fun sucessoNaPrimeiraTentativaNaoTentaDeNovo() =
        runTest {
            var chamadas = 0

            val resultado =
                comRecuoExponencial {
                    chamadas++
                    "ok"
                }

            assertEquals(1, chamadas)
            assertEquals("ok", resultado)
        }

    @Test
    fun tentaDeNovoAposFalhaDeRedeAteConseguir() =
        runTest {
            var chamadas = 0

            val resultado =
                comRecuoExponencial {
                    chamadas++
                    if (chamadas < 3) throw IOException("sem rede") else "ok"
                }

            assertEquals(3, chamadas)
            assertEquals("ok", resultado)
        }

    @Test
    fun tentaDeNovoAposErroHttpAteConseguir() =
        runTest {
            var chamadas = 0

            val resultado =
                comRecuoExponencial {
                    chamadas++
                    if (chamadas < 2) throw httpExceptionDeExemplo() else "ok"
                }

            assertEquals(2, chamadas)
            assertEquals("ok", resultado)
        }

    @Test
    fun paraExatamenteNoMaximoDeTentativasEPropagaAUltimaExcecao() =
        runTest {
            var chamadas = 0
            var erroCapturado: IOException? = null

            try {
                comRecuoExponencial {
                    chamadas++
                    throw IOException("sem rede")
                }
            } catch (e: IOException) {
                erroCapturado = e
            }

            assertEquals(5, chamadas)
            assertNotNull(erroCapturado)
        }

    @Test
    fun respeitaOLimiteDeTentativasPersonalizado() =
        runTest {
            var chamadas = 0

            try {
                comRecuoExponencial(tentativasMaximas = 2) {
                    chamadas++
                    throw IOException("sem rede")
                }
            } catch (e: IOException) {
                // esperado
            }

            assertEquals(2, chamadas)
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun oAtrasoEntreTentativasCresceExponencialmente() =
        runTest {
            var chamadas = 0

            comRecuoExponencial {
                chamadas++
                if (chamadas < 4) throw IOException("sem rede") else "ok"
            }

            // 500 + 1000 + 2000 = 3500ms de espera virtual até a 4ª tentativa.
            assertEquals(3_500L, currentTime)
        }

    @Test
    fun umaExcecaoQueNaoEIoNemHttpPropagaImediatamenteSemNovaTentativa() =
        runTest {
            var chamadas = 0

            try {
                comRecuoExponencial {
                    chamadas++
                    throw IllegalStateException("erro inesperado")
                }
            } catch (e: IllegalStateException) {
                // esperado
            }

            assertEquals(1, chamadas)
        }
}
