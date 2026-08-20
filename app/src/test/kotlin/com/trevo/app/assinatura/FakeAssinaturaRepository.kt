package com.trevo.app.assinatura

import android.app.Activity
import com.trevo.core.data.assinatura.AssinaturaRepository
import com.trevo.core.data.assinatura.EstadoDaAssinatura
import com.trevo.core.data.assinatura.ProdutoDeAssinatura
import com.trevo.core.data.assinatura.isPro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeAssinaturaRepository : AssinaturaRepository {
    private val estado = MutableStateFlow<EstadoDaAssinatura>(EstadoDaAssinatura.Gratuito)
    var produtos: List<ProdutoDeAssinatura> = emptyList()
    val comprasIniciadas = mutableListOf<ProdutoDeAssinatura>()
    var restauracoesChamadas = 0

    fun definirAssinante(productId: String) {
        estado.value = EstadoDaAssinatura.Assinante(productId)
    }

    fun definirGratuito() {
        estado.value = EstadoDaAssinatura.Gratuito
    }

    override fun observarAssinatura(): Flow<EstadoDaAssinatura> = estado.asStateFlow()

    override fun observarIsPro(): Flow<Boolean> = estado.map { it.isPro }

    override suspend fun produtosDisponiveis(): List<ProdutoDeAssinatura> = produtos

    override fun iniciarCompra(
        activity: Activity,
        produto: ProdutoDeAssinatura,
    ) {
        comprasIniciadas += produto
    }

    override suspend fun restaurarCompras() {
        restauracoesChamadas++
    }
}
