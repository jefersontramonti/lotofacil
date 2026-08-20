package com.trevo.app.perfil

import com.trevo.app.MainDispatcherRule
import com.trevo.app.assinatura.FakeAssinaturaRepository
import com.trevo.app.notificacoes.FakeNotificacoesScheduler
import com.trevo.app.preferencias.FakePreferenciasRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.identidade.VerificadorDeIdade
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    companion object {
        private val RELOGIO_FIXO: Clock =
            Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    }

    private data class Ambiente(
        val viewModel: PerfilViewModel,
        val preferencias: FakePreferenciasRepository,
        val scheduler: FakeNotificacoesScheduler,
        val assinatura: FakeAssinaturaRepository,
    )

    // uiState é um StateFlow com SharingStarted.WhileSubscribed — o combine()
    // upstream só roda enquanto houver um coletor ativo, por isso todo teste
    // precisa manter uma coleta em background antes de ler uiState.value.
    private fun TestScope.novoAmbiente(): Ambiente {
        val preferencias = FakePreferenciasRepository()
        val scheduler = FakeNotificacoesScheduler()
        val assinatura = FakeAssinaturaRepository()
        val viewModel =
            PerfilViewModel(
                preferenciasRepository = preferencias,
                scheduler = scheduler,
                validador = ValidadorDataNascimento(RELOGIO_FIXO),
                verificador = VerificadorDeIdade(RELOGIO_FIXO),
                assinaturaRepository = assinatura,
            )
        backgroundScope.launch { viewModel.uiState.collect {} }
        return Ambiente(viewModel, preferencias, scheduler, assinatura)
    }

    @Test
    fun estadoInicialReflenteOPerfilJaSalvo() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil(
                nome = "Marlene",
                nascimento = LocalDate.of(1978, 7, 14),
                signo = Signo.CANCER,
                crencasAtivas = setOf(Crenca.SIGNO, Crenca.LUA),
            )
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals("Marlene", uiState.nome)
            assertEquals("14/07/1978", uiState.nascimento)
            assertEquals(Signo.CANCER, uiState.signo)
            assertEquals(2, uiState.quantidadeDeCrencasAtivas)
        }

    @Test
    fun alterarNomePersisteImediatamente() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", LocalDate.of(1978, 7, 14), Signo.CANCER, emptySet())
            advanceUntilIdle()

            viewModel.aoAlterarNome("Marlene Souza")
            advanceUntilIdle()

            assertEquals("Marlene Souza", preferencias.perfilSalvo.value?.nome)
        }

    @Test
    fun alterarNascimentoParaDataValidaDeMaiorDeIdadeGravaNascimentoESigno() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", LocalDate.of(1978, 7, 14), Signo.CANCER, emptySet())
            advanceUntilIdle()

            viewModel.aoAlterarNascimento("01011990")
            advanceUntilIdle()

            assertEquals(LocalDate.of(1990, 1, 1), preferencias.perfilSalvo.value?.nascimento)
            assertNull(viewModel.uiState.value.erroNascimento)
        }

    @Test
    fun alterarNascimentoParaDataDeMenorDeIdadeNaoSobrescreveOValorSalvo() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            val nascimentoOriginal = LocalDate.of(1978, 7, 14)
            preferencias.salvarPerfil("Marlene", nascimentoOriginal, Signo.CANCER, emptySet())
            advanceUntilIdle()

            viewModel.aoAlterarNascimento("01012020")
            advanceUntilIdle()

            assertEquals(nascimentoOriginal, preferencias.perfilSalvo.value?.nascimento)
            assertEquals(ErroDataNascimento.MENOR_DE_IDADE, viewModel.uiState.value.erroNascimento)
        }

    @Test
    fun ligarLembreteFechamentoPersisteAgendaEEmiteEventoDePermissao() =
        runTest {
            val (viewModel, preferencias, scheduler) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            advanceUntilIdle()

            viewModel.aoAlternarLembreteFechamento(true)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.lembreteFechamentoAtivo)
            assertEquals(LocalTime.of(18, 0), scheduler.horarioLembreteAgendado)
        }

    @Test
    fun desligarLembreteFechamentoCancelaOAgendamento() =
        runTest {
            val (viewModel, preferencias, scheduler) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            advanceUntilIdle()
            viewModel.aoAlternarLembreteFechamento(true)
            advanceUntilIdle()

            viewModel.aoAlternarLembreteFechamento(false)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.lembreteFechamentoAtivo)
            assertTrue(scheduler.lembreteCancelado)
        }

    @Test
    fun escolherHorarioAntesDas20hNaoDisparaAlerta() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            advanceUntilIdle()

            viewModel.aoEscolherHorarioLembrete(LocalTime.of(19, 30))
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.alertaHorarioAposFechamento)
        }

    @Test
    fun escolherHorarioApartirDas20hDisparaAlerta() =
        runTest {
            val (viewModel, preferencias, _) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            advanceUntilIdle()

            viewModel.aoEscolherHorarioLembrete(LocalTime.of(20, 0))
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.alertaHorarioAposFechamento)
        }

    @Test
    fun ligarNotificacaoDeResultadoAgendaOWorker() =
        runTest {
            val (viewModel, preferencias, scheduler) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            advanceUntilIdle()

            viewModel.aoAlternarNotificacaoResultado(true)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.notificacaoResultadoAtiva)
            assertTrue(scheduler.notificacaoResultadoAgendada)
        }

    @Test
    fun assinanteProExpoeIsProEOProductIdDaAssinatura() =
        runTest {
            val (viewModel, preferencias, _, assinatura) = novoAmbiente()
            preferencias.salvarPerfil("Marlene", null, null, emptySet())
            assinatura.definirAssinante("trevo_pro_mensal")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isPro)
            assertEquals("trevo_pro_mensal", viewModel.uiState.value.productIdDaAssinatura)
        }
}
