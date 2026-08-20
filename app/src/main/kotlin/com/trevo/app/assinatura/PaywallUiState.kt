package com.trevo.app.assinatura

import com.trevo.core.data.assinatura.ProdutoDeAssinatura

data class PaywallUiState(
    val carregando: Boolean = true,
    // Vazio quando o Play Console ainda não tem os produtos — a tela mostra
    // "indisponível", nunca um preço inventado (CLAUDE.md §8, por analogia).
    val produtos: List<ProdutoDeAssinatura> = emptyList(),
    val produtoSelecionadoId: String? = null,
) {
    val indisponivel: Boolean get() = !carregando && produtos.isEmpty()
    val produtoSelecionado: ProdutoDeAssinatura?
        get() = produtos.firstOrNull { it.productId == produtoSelecionadoId }
}
