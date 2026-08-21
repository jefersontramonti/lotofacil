package com.trevo.app.home

import com.trevo.app.MainDispatcherRule
import com.trevo.app.assinatura.FakeAssinaturaRepository
import com.trevo.app.palpite.FakePalpiteRepository
import com.trevo.app.preferencias.FakePreferenciasRepository
import com.trevo.app.resultado.FakeResultadoRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.GRUPOS_DO_BICHO
import com.trevo.core.engine.crenca.ModoDeGeracao
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-17T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private val palpiteDeExemplo =
        Palpite(
            dezenas = (1..15).toList(),
            dezenasFixas = emptyList(),
            contribuicoes = emptyMap(),
            forca = 70,
        )

    private fun palpiteComDezenas(dezenas: List<Int>) =
        palpiteDeExemplo.copy(dezenas = dezenas, contribuicoes = emptyMap())

    private fun novoViewModel(
        repository: FakePalpiteRepository = FakePalpiteRepository(RELOGIO_FIXO),
        preferenciasRepository: FakePreferenciasRepository = FakePreferenciasRepository(),
        assinaturaRepository: FakeAssinaturaRepository = FakeAssinaturaRepository(),
        resultadoRepository: FakeResultadoRepository = FakeResultadoRepository(RELOGIO_FIXO),
    ) = HomeViewModel(
        repository,
        preferenciasRepository,
        assinaturaRepository,
        resultadoRepository,
        PalpiteGenerator(Random(1)),
        RELOGIO_FIXO,
    )

    @Test
    fun estadoComecaSemPalpitesQuandoORepositorioEstaVazio() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.palpitesHoje
                    .isEmpty(),
            )
        }

    @Test
    fun palpiteSalvoNoRepositorioApareceNaListaDoDia() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
            assertEquals(
                palpiteDeExemplo.dezenas,
                viewModel.uiState.value.palpitesHoje
                    .first()
                    .dezenas,
            )
        }

    @Test
    fun pedirExclusaoNaoRemoveOPalpiteAteConfirmar() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            val id = repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()

            viewModel.aoPedirExclusao(id)
            advanceUntilIdle()

            assertEquals(id, viewModel.uiState.value.palpiteParaConfirmarExclusao)
            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
        }

    @Test
    fun confirmarExclusaoRemoveOPalpiteELimpaOPedido() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            val id = repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()
            viewModel.aoPedirExclusao(id)

            viewModel.aoConfirmarExclusao()
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.palpitesHoje
                    .isEmpty(),
            )
            assertNull(viewModel.uiState.value.palpiteParaConfirmarExclusao)
        }

    @Test
    fun cancelarExclusaoLimpaOPedidoSemRemoverOPalpite() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            val id = repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()
            viewModel.aoPedirExclusao(id)

            viewModel.aoCancelarExclusao()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.palpiteParaConfirmarExclusao)
            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
        }

    @Test
    fun numeroDoDiaCresceComAOrdemDeCriacaoMasListaVemDoMaisRecenteAoMaisAntigo() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.salvar(palpiteDeExemplo)
            repository.salvar(palpiteDeExemplo)
            advanceUntilIdle()

            val numeros =
                viewModel.uiState.value.palpitesHoje
                    .map { it.numeroDoDia }
            assertEquals(listOf(2, 1), numeros)
        }

    @Test
    fun primeiroPalpiteDoDiaNaoTemDezenasNovas() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.salvar(palpiteComDezenas((1..15).toList()))
            advanceUntilIdle()

            assertNull(
                viewModel.uiState.value.palpitesHoje
                    .single()
                    .dezenasNovas,
            )
        }

    @Test
    fun segundoPalpiteDoDiaMostraSoAsDezenasQueNaoEstavamNoAnterior() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val viewModel = novoViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.salvar(palpiteComDezenas((1..15).toList()))
            repository.salvar(palpiteComDezenas((1..13).toList() + listOf(24, 25)))
            advanceUntilIdle()

            val maisRecente =
                viewModel.uiState.value.palpitesHoje
                    .first()
            assertEquals(listOf(24, 25), maisRecente.dezenasNovas)
        }

    @Test
    fun semPerfilSalvoOsCamposDeSaudacaoFicamNulosENenhumaCrencaSonhoAtiva() =
        runTest {
            val viewModel = novoViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.nome)
            assertNull(viewModel.uiState.value.indiceDeSorte)
            assertNull(viewModel.uiState.value.faseDaLua)
            assertNull(viewModel.uiState.value.signo)
            assertFalse(viewModel.uiState.value.crencaSonhoAtiva)
        }

    @Test
    fun comPerfilSalvoOsCamposDeSaudacaoSaoPreenchidos() =
        runTest {
            val preferencias = FakePreferenciasRepository()
            val viewModel = novoViewModel(preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }

            preferencias.salvarPerfil(
                nome = "Marlene Silva",
                nascimento = LocalDate.of(1978, 7, 14),
                signo = Signo.CANCER,
                crencasAtivas = setOf(Crenca.SIGNO, Crenca.SONHO),
            )
            advanceUntilIdle()

            assertEquals("Marlene Silva", viewModel.uiState.value.nome)
            assertEquals(Signo.CANCER, viewModel.uiState.value.signo)
            assertTrue(viewModel.uiState.value.crencaSonhoAtiva)
            assertEquals(GRUPOS_DO_BICHO.take(10), viewModel.uiState.value.gruposDoSonhoPreview)
        }

    @Test
    fun alternarListaDeGruposInverteOEstadoExpandido() =
        runTest {
            val viewModel = novoViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoAlternarListaDeGrupos()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.listaDeGruposExpandida)

            viewModel.aoAlternarListaDeGrupos()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.listaDeGruposExpandida)
        }

    @Test
    fun abrirEFecharGrupoControlamOGrupoAbertoNoDialog() =
        runTest {
            val viewModel = novoViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoAbrirGrupo(9)
            advanceUntilIdle()
            assertEquals(
                9,
                viewModel.uiState.value.grupoAbertoNoDialog
                    ?.numero,
            )

            viewModel.aoFecharDialogDoSonho()
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.grupoAbertoNoDialog)
        }

    @Test
    fun confirmarSonhoSalvaOGrupoDoDiaEFechaODialog() =
        runTest {
            val preferencias = FakePreferenciasRepository()
            val viewModel = novoViewModel(preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.aoAbrirGrupo(9)
            advanceUntilIdle()

            viewModel.aoConfirmarSonho(9)
            advanceUntilIdle()

            assertEquals(9, viewModel.uiState.value.grupoDoSonhoConfirmadoHoje)
            assertNull(viewModel.uiState.value.grupoAbertoNoDialog)
        }

    @Test
    fun modoPadraoEMisticoESelecionarModoTrocaOEstado() =
        runTest {
            val viewModel = novoViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(ModoDeGeracao.MISTICO, viewModel.uiState.value.modoSelecionado)

            viewModel.aoSelecionarModo(ModoDeGeracao.DESTINO)
            advanceUntilIdle()

            assertEquals(ModoDeGeracao.DESTINO, viewModel.uiState.value.modoSelecionado)
        }

    @Test
    fun gerarNoModoMisticoSalvaUmPalpiteComOModoRegistrado() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            val viewModel = novoViewModel(repository = repository, preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }
            preferencias.salvarPerfil(
                "Marlene",
                LocalDate.of(1978, 7, 14),
                Signo.CANCER,
                setOf(Crenca.SIGNO, Crenca.QUENTES),
            )
            advanceUntilIdle()

            viewModel.aoGerarClick()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
            assertEquals(
                ModoDeGeracao.MISTICO,
                viewModel.uiState.value.palpitesHoje
                    .first()
                    .modo,
            )
        }

    @Test
    fun gerarNoModoMisticoIgnoraCrencasCientistasNaSelecao() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            val viewModel = novoViewModel(repository = repository, preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }
            preferencias.salvarPerfil(
                "Marlene",
                LocalDate.of(1978, 7, 14),
                Signo.CANCER,
                setOf(Crenca.QUENTES, Crenca.ATRASADOS),
            )
            advanceUntilIdle()

            viewModel.aoGerarClick()
            advanceUntilIdle()

            val palpiteSalvo =
                repository.todos.value
                    .first()
                    .palpite
            assertTrue(palpiteSalvo.contribuicoes.isEmpty())
        }

    @Test
    fun semGratisRestanteEForaDoProAoGerarClickNaoSalvaNada() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            preferencias.salvarPerfil("Marlene", null, null, setOf(Crenca.QUENTES))
            val viewModel = novoViewModel(repository = repository, preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoGerarClick()
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)

            // Segundo clique no mesmo dia: já usou o único grátis, não é Pro.
            viewModel.aoGerarClick()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.palpitesHoje.size)
            assertEquals(0, viewModel.uiState.value.palpitesGratisRestantesHoje)
            assertTrue(viewModel.uiState.value.semPalpiteGratisHoje)
        }

    @Test
    fun assinanteProGeraVariasVezesNoMesmoDiaSemConsumirONiHumRestante() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            val assinatura = FakeAssinaturaRepository()
            assinatura.definirAssinante("trevo_pro_anual")
            preferencias.salvarPerfil("Marlene", null, null, setOf(Crenca.QUENTES))
            val viewModel =
                novoViewModel(
                    repository = repository,
                    preferenciasRepository = preferencias,
                    assinaturaRepository = assinatura,
                )
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.aoGerarClick()
            advanceUntilIdle()
            viewModel.aoGerarClick()
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.palpitesHoje.size)
            assertTrue(viewModel.uiState.value.isPro)
            assertFalse(viewModel.uiState.value.semPalpiteGratisHoje)
        }

    @Test
    fun anuncioRecompensadoCreditaUmPalpiteExtraNoDia() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            preferencias.salvarPerfil("Marlene", null, null, setOf(Crenca.QUENTES))
            val viewModel = novoViewModel(repository = repository, preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoGerarClick()
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.palpitesGratisRestantesHoje)

            viewModel.aoAnuncioRecompensado()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.palpitesGratisRestantesHoje)
        }

    @Test
    fun limiteDeDoisAnunciosPorDiaEsgotaOBotaoDeAnuncioENaoCreditaUmTerceiro() =
        runTest {
            val repository = FakePalpiteRepository(RELOGIO_FIXO)
            val preferencias = FakePreferenciasRepository()
            preferencias.salvarPerfil("Marlene", null, null, setOf(Crenca.QUENTES))
            val viewModel = novoViewModel(repository = repository, preferenciasRepository = preferencias)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()
            viewModel.aoGerarClick()
            advanceUntilIdle()
            assertEquals(2, viewModel.uiState.value.anunciosDisponiveisHoje)

            viewModel.aoAnuncioRecompensado()
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.anunciosDisponiveisHoje)

            viewModel.aoAnuncioRecompensado()
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.anunciosDisponiveisHoje)
            assertEquals(2, viewModel.uiState.value.palpitesGratisRestantesHoje)

            viewModel.aoAnuncioRecompensado()
            advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.anunciosDisponiveisHoje)
            assertEquals(2, viewModel.uiState.value.palpitesGratisRestantesHoje)
        }

    @Test
    fun semResultadoAindaBuscadoNumeroDoConcursoCorrenteFicaNulo() =
        runTest {
            val viewModel = novoViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.numeroDoConcursoCorrente)
        }

    @Test
    fun comUltimoResultadoSalvoNumeroDoConcursoCorrenteEUmAMaisQueOUltimoSorteado() =
        runTest {
            val resultadoRepository = FakeResultadoRepository(RELOGIO_FIXO)
            resultadoRepository.adicionarResultado(
                Resultado(
                    numero = 3457,
                    dataApuracao = LocalDate.now(RELOGIO_FIXO),
                    dezenasSorteadas = (1..15).toList(),
                    faixasDePremio = emptyList(),
                    acumulado = false,
                    origem = OrigemDoResultado.API,
                ),
            )
            val viewModel = novoViewModel(resultadoRepository = resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertEquals(3458, viewModel.uiState.value.numeroDoConcursoCorrente)
        }

    @Test
    fun comUltimoResultadoManualSemNumeroNumeroDoConcursoCorrenteFicaNulo() =
        runTest {
            // RF-05.10: entrada manual não tem número real da Caixa — nunca
            // inventa um "próximo" a partir de um número que não existe.
            val resultadoRepository = FakeResultadoRepository(RELOGIO_FIXO)
            resultadoRepository.adicionarResultado(
                Resultado(
                    numero = null,
                    dataApuracao = LocalDate.now(RELOGIO_FIXO),
                    dezenasSorteadas = (1..15).toList(),
                    faixasDePremio = emptyList(),
                    acumulado = false,
                    origem = OrigemDoResultado.MANUAL,
                ),
            )
            val viewModel = novoViewModel(resultadoRepository = resultadoRepository)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.numeroDoConcursoCorrente)
        }
}
