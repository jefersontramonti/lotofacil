package com.trevo.app.di

import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.identidade.VerificadorDeIdade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {
    @Provides
    @Singleton
    fun fornecerRelogio(): Clock = Clock.systemDefaultZone()

    @Provides
    fun fornecerValidadorDataNascimento(clock: Clock): ValidadorDataNascimento = ValidadorDataNascimento(clock)

    @Provides
    fun fornecerVerificadorDeIdade(clock: Clock): VerificadorDeIdade = VerificadorDeIdade(clock)
}
