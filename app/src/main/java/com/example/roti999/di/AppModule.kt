package com.example.roti999.di

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.util.NetworkUtils
import com.example.roti999.util.NotificationHelper
import com.example.roti999.util.Validator
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
        return LocalDatabase(sharedPreferences)
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

    @Provides
    @Singleton
    fun provideValidator(): Validator {
        return Validator()
    }
}