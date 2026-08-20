package com.trevo.core.data.assinatura

import com.android.billingclient.api.Purchase
import org.junit.Assert.assertEquals
import org.junit.Test

class AssinaturaRepositoryImplTest {
    @Test
    fun semCompraNenhumaFicaGratuito() {
        assertEquals(EstadoDaAssinatura.Gratuito, estadoDaAssinaturaDe(emptyList()))
    }

    @Test
    fun comCompraAtivaFicaAssinanteDoProdutoComprado() {
        val compras =
            listOf(
                CompraAtiva(
                    productId = PRODUTO_ID_ANUAL,
                    purchaseState = Purchase.PurchaseState.PURCHASED,
                    purchaseToken = "token-1",
                    isAcknowledged = true,
                ),
            )

        assertEquals(EstadoDaAssinatura.Assinante(PRODUTO_ID_ANUAL), estadoDaAssinaturaDe(compras))
    }

    @Test
    fun comCompraPendenteFicaGratuitoAteConfirmar() {
        val compras =
            listOf(
                CompraAtiva(
                    productId = PRODUTO_ID_MENSAL,
                    purchaseState = Purchase.PurchaseState.PENDING,
                    purchaseToken = "token-2",
                    isAcknowledged = false,
                ),
            )

        assertEquals(EstadoDaAssinatura.Gratuito, estadoDaAssinaturaDe(compras))
    }

    @Test
    fun comCompraCanceladaExpiradaSemAparecerNaListaFicaGratuito() {
        // RF-09.6: nada a mapear aqui além de "não veio na lista" — é assim
        // que queryPurchasesAsync já revoga sozinho (billing é a fonte).
        assertEquals(EstadoDaAssinatura.Gratuito, estadoDaAssinaturaDe(emptyList()))
    }
}
