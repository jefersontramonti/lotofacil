package com.trevo.core.data.di

import com.trevo.core.data.resultado.ResultadoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Duration
import javax.inject.Singleton

// CLAUDE.md §8: única dependência de rede do app. HTTPS obrigatório
// (RNF-04.1) — a URL já é https e não há fallback pra http em lugar
// nenhum deste módulo.
private const val URL_BASE_LOTERIAS_CAIXA = "https://servicebus2.caixa.gov.br/portaldeloterias/api/"

// Achado de auditoria de segurança: sem timeout explícito, o client roda só
// nos defaults implícitos do OkHttp — um comportamento de rede atípico
// deixaria o WorkManager da conferência (ResultadoSorteioWorker) pendurado
// sem limite documentado. 15s cobre uma rede móvel ruim sem competir demais
// com o recuo exponencial (RNF-02.4, até 5 tentativas).
private val TIMEOUT_DE_REDE: Duration = Duration.ofSeconds(15)

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun fornecerJson(): Json = Json { ignoreUnknownKeys = true }

    // BASIC também cobre RNF-08.3 (código de resposta + latência das
    // falhas da API da Caixa) sem precisar de instrumentação própria.
    @Provides
    @Singleton
    fun fornecerOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(TIMEOUT_DE_REDE)
            .readTimeout(TIMEOUT_DE_REDE)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun fornecerRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(URL_BASE_LOTERIAS_CAIXA)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    fun fornecerResultadoApi(retrofit: Retrofit): ResultadoApi = retrofit.create(ResultadoApi::class.java)
}
