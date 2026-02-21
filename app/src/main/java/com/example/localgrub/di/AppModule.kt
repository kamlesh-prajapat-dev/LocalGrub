package com.example.localgrub.di

import android.content.Context
import android.content.SharedPreferences
import com.example.localgrub.data.local.LocalDatabase
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.util.NetworkUtils
import com.example.localgrub.util.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLocalHelper(sharedPreferences: SharedPreferences): LocalDatabase {
        return LocalDatabase(sharedPreferences = sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("GlowPointPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(): NotificationHelper {
        return NotificationHelper()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }
}