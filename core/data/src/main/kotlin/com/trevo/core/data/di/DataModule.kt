package com.trevo.core.data.di

import android.content.Context
import androidx.room.Room
import com.trevo.core.data.TrevoDatabase
import com.trevo.core.data.palpite.PalpiteDao
import com.trevo.core.data.palpite.PalpiteRepository
import com.trevo.core.data.palpite.PalpiteRepositoryImpl
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
    ): TrevoDatabase = Room.databaseBuilder(context, TrevoDatabase::class.java, NOME_DO_BANCO).build()

    @Provides
    fun fornecerPalpiteDao(banco: TrevoDatabase): PalpiteDao = banco.palpiteDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    abstract fun ligarPalpiteRepository(impl: PalpiteRepositoryImpl): PalpiteRepository
}
