package com.trevo.app.di

import com.trevo.core.engine.identidade.ValidadorDataNascimento
import com.trevo.core.engine.identidade.VerificadorDeIdade
import com.trevo.core.engine.palpite.PalpiteGenerator
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

    // Random.Default aqui é o único ponto de produção que o instancia —
    // testes usam PalpiteGenerator(Random(semente)) diretamente, nunca
    // esta função (CLAUDE.md §4).
    @Provides
    fun fornecerGeradorDePalpites(): PalpiteGenerator = PalpiteGenerator()
}
