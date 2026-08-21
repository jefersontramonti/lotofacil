package com.trevo.app.navegacao

import com.trevo.app.MainDispatcherRule
import com.trevo.app.preferencias.FakePreferenciasRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModelTest {
    @get:Rule
    val regraDoDispatcherPrincipal = MainDispatcherRule()

    @Test
    fun `nulo antes da primeira leitura do DataStore`() {
        val preferencias = FakePreferenciasRepository()
        val viewModel = InicioViewModel(preferencias)

        assertNull(viewModel.perfilJaExiste.value)
    }

    @Test
    fun `falso quando nao ha perfil salvo`() =
        runTest {
            val preferencias = FakePreferenciasRepository()
            val viewModel = InicioViewModel(preferencias)
            backgroundScope.launch { viewModel.perfilJaExiste.collect {} }
            advanceUntilIdle()

            assertEquals(false, viewModel.perfilJaExiste.value)
        }

    @Test
    fun `verdadeiro quando ja existe perfil salvo`() =
        runTest {
            val preferencias = FakePreferenciasRepository()
            preferencias.salvarPerfil(
                nome = "Ana",
                nascimento = LocalDate.of(1990, 5, 20),
                signo = null,
                crencasAtivas = emptySet(),
            )
            val viewModel = InicioViewModel(preferencias)
            backgroundScope.launch { viewModel.perfilJaExiste.collect {} }
            advanceUntilIdle()

            assertEquals(true, viewModel.perfilJaExiste.value)
        }
}
