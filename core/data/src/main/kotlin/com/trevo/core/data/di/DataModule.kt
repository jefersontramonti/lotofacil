package com.trevo.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.trevo.core.data.MIGRATION_1_2
import com.trevo.core.data.MIGRATION_2_3
import com.trevo.core.data.TrevoDatabase
import com.trevo.core.data.assinatura.AssinaturaRepository
import com.trevo.core.data.assinatura.AssinaturaRepositoryImpl
import com.trevo.core.data.notificacoes.NotificacoesScheduler
import com.trevo.core.data.notificacoes.NotificacoesSchedulerImpl
import com.trevo.core.data.palpite.PalpiteDao
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteRepositoryImpl
import com.trevo.core.data.preferencias.PreferenciasRepository
import com.trevo.core.data.preferencias.PreferenciasRepositoryImpl
import com.trevo.core.data.preferencias.preferenciasDataStore
import com.trevo.core.data.resultado.ResultadoDao
import com.trevo.core.data.resultado.ResultadoRepository
import com.trevo.core.data.resultado.ResultadoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val NOME_DO_BANCO = "trevo.db"

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun fornecerBancoDeDados(
        @ApplicationContext context: Context,
    ): TrevoDatabase =
        Room
            .databaseBuilder(context, TrevoDatabase::class.java, NOME_DO_BANCO)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun fornecerPalpiteDao(banco: TrevoDatabase): PalpiteDao = banco.palpiteDao()

    @Provides
    fun fornecerResultadoDao(banco: TrevoDatabase): ResultadoDao = banco.resultadoDao()

    @Provides
    @Singleton
    fun fornecerPreferenciasDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.preferenciasDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    abstract fun ligarPalpiteRepository(impl: PalpiteRepositoryImpl): PalpiteRepository

    @Binds
    abstract fun ligarPreferenciasRepository(impl: PreferenciasRepositoryImpl): PreferenciasRepository

    @Binds
    abstract fun ligarResultadoRepository(impl: ResultadoRepositoryImpl): ResultadoRepository

    @Binds
    abstract fun ligarNotificacoesScheduler(impl: NotificacoesSchedulerImpl): NotificacoesScheduler

    @Binds
    abstract fun ligarAssinaturaRepository(impl: AssinaturaRepositoryImpl): AssinaturaRepository
}
