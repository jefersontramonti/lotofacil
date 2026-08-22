package com.trevo.core.data.assinatura

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TENTATIVAS_DE_ACKNOWLEDGE = 3
private const val ATRASO_ENTRE_TENTATIVAS_DE_ACKNOWLEDGE_MS = 500L

// Só o que dá pra saber pelo client (Purchase não expõe data de renovação/
// fim de teste — isso é Play Developer API, servidor). Tipo próprio em vez
// de expor `Purchase` (SDK) pra deixar `estadoDaAssinaturaDe` uma função
// pura, testável sem tocar o BillingClient real (Purchase depende de parsing
// de JSON que não roda em teste de unidade puro).
internal data class CompraAtiva(
    val productId: String,
    val purchaseState: Int,
    val purchaseToken: String,
    val isAcknowledged: Boolean,
)

internal fun estadoDaAssinaturaDe(compras: List<CompraAtiva>): EstadoDaAssinatura {
    val ativa =
        compras.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            ?: return EstadoDaAssinatura.Gratuito
    return EstadoDaAssinatura.Assinante(ativa.productId)
}

private fun Purchase.paraCompraAtiva(): CompraAtiva =
    CompraAtiva(
        productId = products.firstOrNull().orEmpty(),
        purchaseState = purchaseState,
        purchaseToken = purchaseToken,
        isAcknowledged = isAcknowledged,
    )

@Singleton
class AssinaturaRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : AssinaturaRepository {
        // Vive pelo tempo do app (Hilt Singleton) — igual a uma conexão de
        // serviço de sistema, não algo que uma tela liga/desliga; por isso
        // se auto-inicializa no `init`, diferente dos outros repositórios
        // desta camada, que só trabalham quando um ViewModel chama.
        private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val estado = MutableStateFlow<EstadoDaAssinatura>(EstadoDaAssinatura.Gratuito)

        // Populado em produtosDisponiveis(); iniciarCompra() precisa do
        // ProductDetails bruto do SDK, que ProdutoDeAssinatura (tipo de
        // domínio, sem depender do Billing) não carrega.
        private var produtosCache: Map<String, ProductDetails> = emptyMap()

        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { resultado, compras ->
                if (resultado.responseCode == BillingClient.BillingResponseCode.OK) {
                    escopo.launch { processarCompras(compras.orEmpty()) }
                }
            }

        private val billingClient: BillingClient =
            BillingClient
                .newBuilder(context)
                .setListener(purchasesUpdatedListener)
                // Billing 7.1.1 exige essa declaração pra construir os params,
                // mesmo o Trevo só vendendo assinatura (nenhum produto
                // avulso/one-time) — sem isso, o builder lança
                // IllegalArgumentException em tempo de execução.
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build()

        init {
            escopo.launch { sincronizarComprasAtivas() }
            // Achado de auditoria de segurança: sem isso, expiração/reembolso/
            // cancelamento feitos na Play Store sem fechar o app deixavam o
            // Pro preso até o processo morrer ou o usuário tocar em "restaurar
            // compras" manualmente. `ProcessLifecycleOwner` cobre o app como
            // um todo (não uma Activity específica) — `addObserver` precisa
            // rodar na main thread, por isso o `Dispatchers.Main.immediate`
            // aqui, separado do `escopo` (que é IO).
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
                ProcessLifecycleOwner.get().lifecycle.addObserver(
                    object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) {
                            escopo.launch { sincronizarComprasAtivas() }
                        }
                    },
                )
            }
        }

        private suspend fun sincronizarComprasAtivas() {
            garantirConectado()
            processarCompras(consultarComprasAtivas())
        }

        override fun observarAssinatura(): Flow<EstadoDaAssinatura> = estado.asStateFlow()

        override fun observarIsPro(): Flow<Boolean> = estado.map { it.isPro }

        override suspend fun produtosDisponiveis(): List<ProdutoDeAssinatura> {
            garantirConectado()
            val params =
                QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(
                        listOf(PRODUTO_ID_MENSAL, PRODUTO_ID_ANUAL).map { id ->
                            QueryProductDetailsParams.Product
                                .newBuilder()
                                .setProductId(id)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                        },
                    ).build()
            val resultado = billingClient.queryProductDetails(params)
            val detalhes = resultado.productDetailsList.orEmpty()
            produtosCache = detalhes.associateBy { it.productId }
            return detalhes.mapNotNull { it.paraProdutoDeAssinatura() }
        }

        override fun iniciarCompra(
            activity: Activity,
            produto: ProdutoDeAssinatura,
        ) {
            val detalhes = produtosCache[produto.productId] ?: return
            val params =
                BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams
                                .newBuilder()
                                .setProductDetails(detalhes)
                                .setOfferToken(produto.ofertaToken)
                                .build(),
                        ),
                    ).build()
            billingClient.launchBillingFlow(activity, params)
        }

        override suspend fun restaurarCompras() = sincronizarComprasAtivas()

        private suspend fun processarCompras(compras: List<Purchase>) {
            val ativa = compras.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            if (ativa != null && !ativa.isAcknowledged) {
                confirmarCompra(ativa)
            }
            estado.value = estadoDaAssinaturaDe(compras.map { it.paraCompraAtiva() })
        }

        // Achado de auditoria de segurança: o resultado do acknowledge era
        // descartado, então uma falha (rede instável, serviço fora do ar)
        // passava em silêncio — e uma compra não reconhecida em 3 dias é
        // reembolsada automaticamente pelo Google. `PURCHASED` já libera o
        // Pro (estadoDaAssinaturaDe) independente disto dar certo; tenta
        // algumas vezes aqui mesmo e, se ainda assim falhar, a compra
        // continua "não reconhecida" — a próxima `processarCompras()`
        // (abertura do app, restaurar, ou volta do background) tenta de
        // novo, nunca fica só nesta tentativa.
        private suspend fun confirmarCompra(compra: Purchase) {
            repeat(TENTATIVAS_DE_ACKNOWLEDGE) { tentativa ->
                val resultado =
                    billingClient.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(compra.purchaseToken).build(),
                    )
                if (resultado.responseCode == BillingClient.BillingResponseCode.OK) return
                if (tentativa < TENTATIVAS_DE_ACKNOWLEDGE - 1) delay(ATRASO_ENTRE_TENTATIVAS_DE_ACKNOWLEDGE_MS)
            }
        }

        private suspend fun consultarComprasAtivas(): List<Purchase> {
            val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
            return billingClient.queryPurchasesAsync(params).purchasesList
        }

        private suspend fun garantirConectado() {
            if (billingClient.isReady) return
            suspendCancellableCoroutine { continuacao ->
                billingClient.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (continuacao.isActive) continuacao.resume(Unit)
                        }

                        override fun onBillingServiceDisconnected() {
                            // Próxima chamada reconecta sozinha (billingClient.isReady == false).
                        }
                    },
                )
            }
        }

        private fun ProductDetails.paraProdutoDeAssinatura(): ProdutoDeAssinatura? {
            val oferta = subscriptionOfferDetails?.firstOrNull() ?: return null
            val faseDePreco = oferta.pricingPhases.pricingPhaseList.maxByOrNull { it.priceAmountMicros } ?: return null
            return ProdutoDeAssinatura(
                productId = productId,
                precoFormatado = faseDePreco.formattedPrice,
                ofertaToken = oferta.offerToken,
            )
        }
    }
