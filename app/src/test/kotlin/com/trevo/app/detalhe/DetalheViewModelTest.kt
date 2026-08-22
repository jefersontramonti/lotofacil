package com.trevo.app.detalhe

import androidx.lifecycle.SavedStateHandle
import com.trevo.app.MainDispatcherRule
import com.trevo.app.assinatura.FakeAssinaturaRepository
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.app.preferencias.FakePreferenciasRepository
import com.trevo.app.resultado.FakeResultadoRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.palpite.Palpite
import com.trevo.core.engine.palpite.PalpiteGenerator
import com.trevo.core.engine.resultado.OrigemDoResultado
import com.trevo.core.engine.resultado.Resultado
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class DetalheViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-17T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private val palpiteDeExemplo =
        Palpite(
            dezenas = (1..15).toList(),
            dezenasFixas = listOf(1, 2),
            contribuicoes = mapOf(Crenca.SIGNO to listOf(1, 2, 3), Crenca.LUA to listOf(4, 5)),
            forca = 80,
        )

    private fun novoViewModel(
        palpiteId: Long,
        repository: FakePalpiteRepository = FakePalpiteRepository(RELOGIO_FIXO),
        preferenciasRepository: FakePreferenciasRepository = FakePreferenciasRepository(),
        resultadoRepository: FakeResultadoRepository = FakeResultadoRepository(RELOGIO_FIXO),
        assinaturaRepository: FakeAssinaturaRepository = FakeAssinaturaRepository(),
        gerador: PalpiteGenerator = PalpiteGenerator(Random(1)),
    ) = DetalheViewModel(
        savedStateHandle = SavedStateHandle(mapOf("palpiteId" to palpiteId)),
        repository = repository,
        preferenciasRepository = preferenciasRepository,
        resultadoRepository = resultadoRepository,
        assinaturaRepository = assinaturaRepository,
        gerador = gerador,
        clock = RELOGIO_FIXO,
    )

    @Test
    fun palpiteInexistenteDeixaPalpiteExisteFalso() =
        runTest {
            val viewModel = novoViewModel(palpiteId = 999)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.palpiteExiste)
        }

    @Test
    fun palpiteExistenteExpoeDezenasForcaOrigensEEstatisticas() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val estado = viewModel.uiState.value
            assertTrue(estado.palpiteExiste)
            assertEquals(palpiteDeExemplo.dezenas, estado.dezenas)
            assertEquals(80, estado.forca)
            assertEquals(setOf(Crenca.SIGNO, Crenca.LUA), estado.origens.map { it.crenca }.toSet())
            assertEquals((1..15).sum(), estado.soma)
            assertEquals(1, estado.numeroDoDia)
            assertEquals(3_268_760L, estado.chanceRealUmEm)
            assertFalse(estado.podeVerDesdobramentos)
        }

    @Test
    fun entrarNoModoEdicaoPopulaDezenasEmEdicaoComAsDezenasAtuais() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoEntrarNoModoEdicao()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.modoEdicao)
            assertEquals(palpiteDeExemplo.dezenas.toSet(), viewModel.uiState.value.dezenasEmEdicao)
        }

    @Test
    fun tocarUmaDezenaJaFixaNaEdicaoNaoAlteraNada() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoEntrarNoModoEdicao()
            advanceUntilIdle()

            viewModel.aoTocarDezenaNaEdicao(1)
            advanceUntilIdle()

            assertTrue(1 in viewModel.uiState.value.dezenasEmEdicao)
        }

    @Test
    fun tocarUmaDezenaNaoFixaAlternaNaSelecao() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoEntrarNoModoEdicao()
            advanceUntilIdle()

            viewModel.aoTocarDezenaNaEdicao(15)
            advanceUntilIdle()
            assertFalse(15 in viewModel.uiState.value.dezenasEmEdicao)

            viewModel.aoTocarDezenaNaEdicao(20)
            advanceUntilIdle()
            assertTrue(20 in viewModel.uiState.value.dezenasEmEdicao)
        }

    @Test
    fun salvarEdicaoComContagemErradaNaoPersisteEContinuaEmEdicao() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoEntrarNoModoEdicao()
            viewModel.aoTocarDezenaNaEdicao(20)
            advanceUntilIdle()

            viewModel.aoSalvarEdicao()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.modoEdicao)
            assertEquals(palpiteDeExemplo.dezenas, viewModel.uiState.value.dezenas)
        }

    @Test
    fun salvarEdicaoComContagemCertaPersisteASubstituicao() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoEntrarNoModoEdicao()
            viewModel.aoTocarDezenaNaEdicao(15)
            viewModel.aoTocarDezenaNaEdicao(20)
            advanceUntilIdle()

            viewModel.aoSalvarEdicao()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.modoEdicao)
            assertEquals((1..14).toList() + 20, viewModel.uiState.value.dezenas)
        }

    @Test
    fun salvarEdicaoComGuardarFixasMarcadoFundeAsDezenasNovasNasFixas() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoEntrarNoModoEdicao()
            viewModel.aoTocarDezenaNaEdicao(15)
            viewModel.aoTocarDezenaNaEdicao(20)
            viewModel.aoAlternarGuardarComoFixas()
            advanceUntilIdle()

            viewModel.aoSalvarEdicao()
            advanceUntilIdle()

            assertEquals(listOf(1, 2, 20), viewModel.uiState.value.dezenasFixas)
        }

    @Test
    fun limparFixasZeraAsDezenasFixas() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoLimparFixas()
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.dezenasFixas
                    .isEmpty(),
            )
        }

    @Test
    fun refazerMantemAsMesmasCrencasEDezenasFixasEProduzOMesmoResultadoDoGeradorDireto() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            preferencias.salvarPerfil(
                nome = "Marlene",
                nascimento = LocalDate.of(1978, 7, 14),
                signo = Signo.CANCER,
                crencasAtivas = setOf(Crenca.SIGNO, Crenca.LUA),
            )
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel =
                novoViewModel(
                    palpiteId = id,
                    repository = repository,
                    preferenciasRepository = preferencias,
                    gerador = PalpiteGenerator(Random(7)),
                )
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoRefazer()
            advanceUntilIdle()

            val esperado =
                PalpiteGenerator(Random(7)).gerar(
                    crencasAtivas = setOf(Crenca.SIGNO, Crenca.LUA),
                    dados =
                        com.trevo.core.engine.crenca.DadosDeContribuicao(
                            hoje = LocalDate.now(RELOGIO_FIXO),
                            nascimento = LocalDate.of(1978, 7, 14),
                            signo = Signo.CANCER,
                            nome = "Marlene",
                            grupoDoSonho = null,
                        ),
                    dezenasFixas = setOf(1, 2),
                    quantidade = 15,
                )
            assertEquals(esperado.dezenas, viewModel.uiState.value.dezenas)
            assertEquals(
                setOf(Crenca.SIGNO, Crenca.LUA),
                viewModel.uiState.value.origens
                    .map { it.crenca }
                    .toSet(),
            )
            assertEquals(listOf(1, 2), viewModel.uiState.value.dezenasFixas)
        }

    @Test
    fun pedirCancelarEConfirmarExclusaoControlamOEstadoDoDialogo() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoPedirExclusao()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.palpiteParaConfirmarExclusao)

            viewModel.aoCancelarExclusao()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.palpiteParaConfirmarExclusao)

            viewModel.aoConfirmarExclusao()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.palpiteExiste)
        }

    @Test
    fun abrirFecharECopiarControlamOEstadoDeCompartilhamento() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoAbrirCompartilharClick()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.compartilhando)
            assertFalse(viewModel.uiState.value.copiado)

            viewModel.aoMarcarCopiadoClick()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.copiado)

            viewModel.aoFecharCompartilharClick()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.compartilhando)
            assertFalse(viewModel.uiState.value.copiado)
        }

    @Test
    fun semResultadoCasadoComODiaDoPalpiteNumeroDoConcursoFicaNulo() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel = novoViewModel(palpiteId = id, repository = repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.numeroDoConcurso)
        }

    @Test
    fun comResultadoCasadoComODiaDoPalpiteExpoeONumeroDoConcurso() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val resultadoRepository = FakeResultadoRepository(RELOGIO_FIXO)
            val id = repository.salvar(palpiteDeExemplo)
            resultadoRepository.adicionarResultado(
                Resultado(
                    numero = 3457,
                    dataApuracao = LocalDate.now(RELOGIO_FIXO),
                    dezenasSorteadas = (1..15).toList(),
                    faixasDePremio = emptyList(),
                    acumulado = false,
                    origem = OrigemDoResultado.API,
                    proximoConcurso = null,
                ),
            )
            val viewModel =
                novoViewModel(palpiteId = id, repository = repository, resultadoRepository = resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(3457, viewModel.uiState.value.numeroDoConcurso)
        }

    @Test
    fun isProReflenteDaAssinaturaRepositoryDestravaOFechamento() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val assinaturaRepository = FakeAssinaturaRepository()
            val id = repository.salvar(palpiteDeExemplo)
            val viewModel =
                novoViewModel(palpiteId = id, repository = repository, assinaturaRepository = assinaturaRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isPro)

            assinaturaRepository.definirAssinante("trevo_pro_anual")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isPro)
        }
}
