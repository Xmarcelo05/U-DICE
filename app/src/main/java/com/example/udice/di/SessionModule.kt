package com.example.udice.di

import com.example.udice.BuildConfig
import com.example.udice.data.repository.CloudSessionRepository
import com.example.udice.data.repository.SessionRepository
import com.example.udice.data.repository.SessionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    @Provides
    @Singleton
    fun provideSession(
        cloud: Provider<CloudSessionRepository>,
        local: Provider<SessionRepositoryImpl>
    ): SessionRepository = if (BuildConfig.UDICE_SUPABASE) cloud.get() else local.get()
}
