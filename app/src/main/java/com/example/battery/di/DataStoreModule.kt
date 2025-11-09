package com.example.battery.di

import android.content.Context
import com.example.battery.data.datastore.ConfigDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideConfigDataStore(
        @ApplicationContext context: Context
    ): ConfigDataStore {
        return ConfigDataStore(context)
    }
}