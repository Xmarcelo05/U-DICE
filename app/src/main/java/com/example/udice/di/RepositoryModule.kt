package com.example.udice.di

import com.example.udice.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
    @Binds @Singleton abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
    @Binds @Singleton abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
    @Binds @Singleton abstract fun bindSavedPostsRepository(impl: SavedPostsRepositoryImpl): SavedPostsRepository
}
