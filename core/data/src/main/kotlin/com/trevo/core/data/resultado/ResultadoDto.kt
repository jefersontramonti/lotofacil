package com.trevo.core.data.resultado

import kotlinx.serialization.Serializable

// Schema real confirmado contra a API oficial da Caixa
// (servicebus2.caixa.gov.br/portaldeloterias/api/lotofacil) — não
// documentado em nenhum doc do projeto, verificado via requisição real.
@Serializable
data class ResultadoDto(
    val numero: Int,
    val dataApuracao: String,
    val listaDezenas: List<String>,
    val listaRateioPremio: List<RateioDto> = emptyList(),
    val acumulado: Boolean = false,
)

@Serializable
data class RateioDto(
    val faixa: Int,
    val numeroDeGanhadores: Long,
    val valorPremio: Double,
)
