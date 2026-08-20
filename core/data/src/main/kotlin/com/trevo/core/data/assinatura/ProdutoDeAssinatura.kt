package com.trevo.core.data.assinatura

// RF-09.4 — os dois planos oferecidos. `PRODUTO_ID_MENSAL`/`PRODUTO_ID_ANUAL`
// precisam bater exatamente com os product id + base plan id criados no
// Play Console (ver PROJECT_STATE.md, nota de RF-09) — sem isso configurado,
// AssinaturaRepository.produtosDisponiveis() devolve lista vazia, nunca um
// preço inventado.
const val PRODUTO_ID_MENSAL = "trevo_pro_mensal"
const val PRODUTO_ID_ANUAL = "trevo_pro_anual"

data class ProdutoDeAssinatura(
    val productId: String,
    val precoFormatado: String,
    val ofertaToken: String,
)
