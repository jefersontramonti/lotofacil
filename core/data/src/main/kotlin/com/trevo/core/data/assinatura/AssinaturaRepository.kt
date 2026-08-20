package com.trevo.core.data.assinatura

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface AssinaturaRepository {
    fun observarAssinatura(): Flow<EstadoDaAssinatura>

    fun observarIsPro(): Flow<Boolean>

    // Vazio quando o Play Console ainda não tem os produtos configurados —
    // nunca inventa preço (ver ProdutoDeAssinatura).
    suspend fun produtosDisponiveis(): List<ProdutoDeAssinatura>

    fun iniciarCompra(
        activity: Activity,
        produto: ProdutoDeAssinatura,
    )

    // RF-09.7 — reconsulta as compras ativas na conta Google atual (mesmo
    // mecanismo que já roda ao conectar); usado pro botão explícito de
    // restaurar, não só na abertura do app.
    suspend fun restaurarCompras()
}
