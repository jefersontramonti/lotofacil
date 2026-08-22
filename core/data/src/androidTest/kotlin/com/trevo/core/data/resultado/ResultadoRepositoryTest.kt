package com.trevo.core.data.resultado

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.trevo.core.data.TrevoDatabase
import com.trevo.core.engine.resultado.OrigemDoResultado
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class ResultadoRepositoryTest {
    private val relogioFixo = Clock.fixed(Instant.parse("2026-08-17T12:00:00Z"), ZoneOffset.UTC)

    private lateinit var banco: TrevoDatabase
    private lateinit var repositorio: ResultadoRepository
    private lateinit var apiFake: ApiFake

    // Testa o mapeamento e a persistência do repositório sem depender da
    // rede de verdade — o contrato real da API foi verificado à parte
    // (WebFetch, documentado no plano da sessão) e é exercitado de ponta a
    // ponta rodando o app no emulador (tem internet real).
    private class ApiFake : ResultadoApi {
        var proximoResultado: ResultadoDto? = null
        var chamadas = 0

        override suspend fun buscarUltimo(): ResultadoDto {
            chamadas++
            return proximoResultado ?: error("sem resultado configurado no fake")
        }

        override suspend fun buscarPorNumero(numero: Int): ResultadoDto = buscarUltimo()
    }

    @Before
    fun montarBancoEmMemoria() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        banco = Room.inMemoryDatabaseBuilder(context, TrevoDatabase::class.java).allowMainThreadQueries().build()
        apiFake = ApiFake()
        repositorio = ResultadoRepositoryImpl(api = apiFake, dao = banco.resultadoDao(), clock = relogioFixo)
    }

    @After
    fun fecharBanco() {
        banco.close()
    }

    private val dtoDeExemplo =
        ResultadoDto(
            numero = 3457,
            dataApuracao = "31/07/2025",
            listaDezenas = (1..15).map { "%02d".format(it) },
            listaRateioPremio =
                listOf(
                    RateioDto(faixa = 1, numeroDeGanhadores = 0, valorPremio = 0.0),
                    RateioDto(faixa = 3, numeroDeGanhadores = 6627, valorPremio = 35.0),
                ),
            acumulado = true,
        )

    @Test
    fun buscarUltimoResultadoMapeiaOResultadoDaApi() =
        runTest {
            apiFake.proximoResultado = dtoDeExemplo

            val resultado = repositorio.buscarUltimoResultado()

            assertEquals(3457, resultado.numero)
            assertEquals((1..15).toList(), resultado.dezenasSorteadas)
            assertEquals(OrigemDoResultado.API, resultado.origem)
            assertEquals(1, apiFake.chamadas)
            assertNull(resultado.proximoConcurso)
        }

    @Test
    fun apiTrazDadosDoProximoConcursoESobrevivemAoRoundTripDoRoom() =
        runTest {
            apiFake.proximoResultado =
                dtoDeExemplo.copy(
                    numeroConcursoProximo = 3458,
                    dataProximoConcurso = "02/08/2025",
                    valorEstimadoProximoConcurso = 1700000.0,
                    valorAcumuladoProximoConcurso = 1556187.62,
                )

            val resultado = repositorio.buscarUltimoResultado()
            val salvo = repositorio.observarUltimoResultadoSalvo().first()

            val proximo = checkNotNull(resultado.proximoConcurso)
            assertEquals(3458, proximo.numero)
            assertEquals(LocalDate.of(2025, 8, 2), proximo.data)
            assertEquals(0, java.math.BigDecimal("1700000.0").compareTo(proximo.valorEstimadoPremio))
            assertEquals(0, java.math.BigDecimal("1556187.62").compareTo(proximo.valorAcumulado))
            assertEquals(3458, salvo?.proximoConcurso?.numero)
        }

    @Test
    fun apiTrazAFaixaDe13AcertosComOValorCorreto() =
        runTest {
            apiFake.proximoResultado = dtoDeExemplo

            val resultado = repositorio.buscarUltimoResultado()

            val faixa13 = resultado.faixasDePremio.first { it.acertosNecessarios == 13 }
            assertEquals(6627L, faixa13.numeroDeGanhadores)
            assertEquals(0, java.math.BigDecimal("35.00").compareTo(faixa13.valorPremio))
        }

    @Test
    fun apoisBuscarObservarUltimoResultadoSalvoDevolveOMesmoResultado() =
        runTest {
            apiFake.proximoResultado = dtoDeExemplo

            repositorio.buscarUltimoResultado()
            val salvo = repositorio.observarUltimoResultadoSalvo().first()

            assertEquals(3457, salvo?.numero)
        }

    @Test
    fun semNadaSalvoObservarUltimoResultadoSalvoDevolveNulo() =
        runTest {
            assertNull(repositorio.observarUltimoResultadoSalvo().first())
        }

    @Test
    fun salvarResultadoManualNaoTemNumeroNemFaixasDePremio() =
        runTest {
            repositorio.salvarResultadoManual((1..15).toSet())

            val salvo = repositorio.observarUltimoResultadoSalvo().first()

            assertNull(salvo?.numero)
            assertTrue(salvo?.faixasDePremio?.isEmpty() == true)
            assertEquals(OrigemDoResultado.MANUAL, salvo?.origem)
            assertEquals((1..15).toList(), salvo?.dezenasSorteadas)
        }
}
