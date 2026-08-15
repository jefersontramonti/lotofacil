package com.trevo.app.onboarding

import androidx.lifecycle.ViewModel
import com.trevo.core.engine.identidade.ErroDataNascimento
import com.trevo.core.engine.identidade.ResultadoDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.identidade.VerificadorDeIdade
import com.trevo.core.engine.identidade.formatarDataNascimento
import com.trevo.core.engine.identidade.signoDe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class IdentidadeViewModel
    @Inject
    constructor(
        private val validador: ValidadorDataNascimento,
        private val verificador: VerificadorDeIdade,
    ) : ViewModel() {
        private val estado = MutableStateFlow(IdentidadeUiState())
        val uiState: StateFlow<IdentidadeUiState> = estado.asStateFlow()

        fun aoAlterarNome(valor: String) {
            estado.value = estado.value.copy(nome = valor)
        }

        fun aoAlterarNascimento(valor: String) {
            val nascimentoFormatado = formatarDataNascimento(valor)
            val resultado = validador.validar(nascimentoFormatado)
            estado.value =
                estado.value.copy(
                    nascimento = nascimentoFormatado,
                    nascimentoValido = resultado is ResultadoDataNascimento.Valida,
                    erroNascimento = erroExibivel(resultado),
                    signo = signoExibivel(resultado),
                )
        }

        private fun erroExibivel(resultado: ResultadoDataNascimento): ErroDataNascimento? =
            when (resultado) {
                is ResultadoDataNascimento.Valida ->
                    if (verificador.ehMaiorDeIdade(resultado.data)) null else ErroDataNascimento.MENOR_DE_IDADE
                is ResultadoDataNascimento.Invalida ->
                    resultado.erro.takeUnless { it == ErroDataNascimento.VAZIO }
            }

        // Signo depende só de a data ser validamente formada — a checagem de
        // idade (erroExibivel) é um julgamento independente sobre o mesmo
        // LocalDate.
        private fun signoExibivel(resultado: ResultadoDataNascimento): Signo? =
            when (resultado) {
                is ResultadoDataNascimento.Valida -> signoDe(resultado.data)
                is ResultadoDataNascimento.Invalida -> null
            }
    }
