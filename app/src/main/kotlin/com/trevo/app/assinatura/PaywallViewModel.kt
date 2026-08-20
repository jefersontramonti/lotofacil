package com.trevo.app.assinatura

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trevo.core.data.assinatura.AssinaturaRepository
import com.trevo.core.data.assinatura.PRODUTO_ID_ANUAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel
    @Inject
    constructor(
        private val assinaturaRepository: AssinaturaRepository,
    ) : ViewModel() {
        private val estado = MutableStateFlow(PaywallUiState())
        val uiState: StateFlow<PaywallUiState> = estado.asStateFlow()

        init {
            viewModelScope.launch {
                val produtos = assinaturaRepository.produtosDisponiveis()
                // RF-09.4 — Anual é o padrão selecionado (wireframe 1n, "Mais escolhido").
                val selecionado = produtos.firstOrNull { it.productId == PRODUTO_ID_ANUAL } ?: produtos.firstOrNull()
                estado.value =
                    estado.value.copy(
                        carregando = false,
                        produtos = produtos,
                        produtoSelecionadoId = selecionado?.productId,
                    )
            }
        }

        fun aoEscolherPlano(productId: String) {
            estado.value = estado.value.copy(produtoSelecionadoId = productId)
        }

        // RF-09.4/09.6 — a confirmação/liberação em si vem do
        // PurchasesUpdatedListener do BillingClient (AssinaturaRepositoryImpl),
        // não daqui: este método só abre o fluxo de compra do Google.
        fun aoComecarTesteClick(activity: Activity) {
            val produto = uiState.value.produtoSelecionado ?: return
            assinaturaRepository.iniciarCompra(activity, produto)
        }
    }
