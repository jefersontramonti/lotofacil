package com.trevo.core.data.resultado

import retrofit2.http.GET
import retrofit2.http.Path

interface ResultadoApi {
    @GET("lotofacil")
    suspend fun buscarUltimo(): ResultadoDto

    @GET("lotofacil/{numero}")
    suspend fun buscarPorNumero(
        @Path("numero") numero: Int,
    ): ResultadoDto
}
