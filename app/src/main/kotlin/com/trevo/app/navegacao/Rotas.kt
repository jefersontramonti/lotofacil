package com.trevo.app.navegacao

object Rotas {
    const val ABERTURA = "abertura"
    const val IDENTIDADE = "identidade"
    const val CRENCAS = "crencas"
    const val GERANDO = "gerando"
    const val HOME = "home"
    const val CONFERENCIA = "conferencia"
    const val HISTORICO = "historico"
    const val DETALHE = "detalhe/{palpiteId}"
    const val DESDOBRAMENTOS = "desdobramentos/{palpiteId}"

    fun detalhe(palpiteId: Long) = "detalhe/$palpiteId"

    fun desdobramentos(palpiteId: Long) = "desdobramentos/$palpiteId"
}
