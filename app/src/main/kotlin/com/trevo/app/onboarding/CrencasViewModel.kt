package com.trevo.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.engine.crenca.Crenca
import com.trevo.core.engine.crenca.DadosDeContribuicao
import com.trevo.core.engine.identidade.ResultadoDataNascimento
import com.trevo.core.engine.identidade.Signo
import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.palpite.PalpiteGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CrencasViewModel
    @Inject
    constructor(
        private val gerador: PalpiteGenerator,
        private val validadorDeNascimento: ValidadorDataNascimento,
        private val repository: PalpiteRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val estado = MutableStateFlow(CrencasUiState())
        val uiState: StateFlow<CrencasUiState> = estado.asStateFlow()

        fun aoTocarCrenca(crenca: Crenca) {
            val selecaoAtual = estado.value.selecionadas
            val novaSelecao = if (crenca in selecaoAtual) selecaoAtual - crenca else selecaoAtual + crenca
            estado.value = estado.value.copy(selecionadas = novaSelecao)
        }

        // `nascimentoTexto` chega bruto (o mesmo texto formatado do campo de
        // RF-01.3/RF-01.9) e é revalidado aqui — a mesma fonte da verdade de
        // parsing usada em IdentidadeViewModel, nunca um parser paralelo.
        fun aoGerarPalpite(
            nome: String,
            nascimentoTexto: String,
            signo: Signo?,
        ) {
            val resultado = validadorDeNascimento.validar(nascimentoTexto)
            val nascimento = (resultado as? ResultadoDataNascimento.Valida)?.data
            val dados =
                DadosDeContribuicao(
                    hoje = LocalDate.now(clock),
                    nascimento = nascimento,
                    signo = signo,
                    nome = nome,
                )
            val palpite = gerador.gerar(crencasAtivas = estado.value.selecionadas, dados = dados)
            estado.value = estado.value.copy(palpiteGerado = palpite)
            // Salva assíncrono: a Home observa o repositório por Flow, então
            // navegar pra lá antes do insert terminar não perde o palpite —
            // o card aparece assim que a escrita completar (RF-03/home).
            viewModelScope.launch { repository.salvar(palpite) }
        }
    }
