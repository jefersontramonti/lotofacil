package com.trevo.core.data.di

import org.junit.Assert.assertTrue
import org.junit.Test

// Achado de auditoria de segurança: sem timeout explícito, o OkHttpClient
// roda só nos defaults implícitos da lib. Este teste existe pra travar a
// regressão — se alguém remover connectTimeout()/readTimeout() do builder,
// o teste quebra.
class NetworkModuleTest {
    @Test
    fun okHttpClientTemTimeoutDeConexaoEDeLeituraExplicitos() {
        val client = NetworkModule.fornecerOkHttpClient()

        assertTrue(client.connectTimeoutMillis > 0)
        assertTrue(client.readTimeoutMillis > 0)
    }
}
