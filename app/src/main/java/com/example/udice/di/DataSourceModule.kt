package com.example.udice.di

import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.local.RoomLocalDataSource
import com.example.udice.data.remote.FakeRemoteDataSource
import com.example.udice.data.remote.RemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds @Singleton abstract fun bindLocalDataSource(impl: RoomLocalDataSource): LocalDataSource
    @Binds @Singleton abstract fun bindRemoteDataSource(impl: FakeRemoteDataSource): RemoteDataSource
}
