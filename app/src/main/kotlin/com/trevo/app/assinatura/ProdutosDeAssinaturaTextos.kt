package com.trevo.app.assinatura

import com.trevo.app.R
import com.trevo.core.data.assinatura.PRODUTO_ID_ANUAL
import com.trevo.core.data.assinatura.PRODUTO_ID_MENSAL

// Nome de exibição por productId — usado no Perfil (RF-07.8) e no Paywall
// (RF-09.4/09.5), pra não duplicar o mapeamento nas duas telas.
fun nomeDoPlano(productId: String): Int =
    when (productId) {
        PRODUTO_ID_ANUAL -> R.string.assinatura_plano_anual_nome
        PRODUTO_ID_MENSAL -> R.string.assinatura_plano_mensal_nome
        else -> R.string.assinatura_plano_desconhecido_nome
    }
