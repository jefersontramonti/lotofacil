package com.trevo.core.data.assinatura

// RF-09.6 — deriva sempre de `BillingClient.queryPurchasesAsync`, nunca de
// uma flag local solta: se o Billing não devolve mais a compra, o estado
// volta pra Gratuito sozinho, sem revogação manual. O client não tem acesso
// a data de renovação/fim de teste (isso é Play Developer API, servidor) —
// não inventamos essa data (CLAUDE.md §8 aplicado por analogia a preço/data
// de cobrança).
sealed interface EstadoDaAssinatura {
    data object Gratuito : EstadoDaAssinatura

    data class Assinante(
        val productId: String,
    ) : EstadoDaAssinatura
}

val EstadoDaAssinatura.isPro: Boolean get() = this is EstadoDaAssinatura.Assinante
