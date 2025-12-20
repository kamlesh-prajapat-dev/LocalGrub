package com.example.roti999.di

import android.content.Context
import androidx.work.WorkManager
import com.example.roti999.data.local.LocalDatabase
import com.example.roti999.domain.repository.AuthRepository
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.domain.repository.NotificationRepository
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.repository.OwnerRepository
import com.example.roti999.domain.repository.UserRepository
import com.example.roti999.domain.usecase.AuthUseCase
import com.example.roti999.domain.usecase.DishesUseCase
import com.example.roti999.domain.usecase.OrderUseCase
import com.example.roti999.domain.usecase.UserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {

    @Provides
    @Singleton
    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase {
        return AuthUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideDishesUseCase(dishesRepository: DishesRepository): DishesUseCase {
        return DishesUseCase(dishesRepository)
    }

    @Provides
    @Singleton
    fun provideOrderUseCase(
        orderRepository: OrderRepository,
        workManager: WorkManager,
        ownerRepository: OwnerRepository,
        notificationRepository: NotificationRepository
    ): OrderUseCase {
        return OrderUseCase(
            orderRepository,
            workManager = workManager,
            ownerRepository = ownerRepository,
            notificationRepository = notificationRepository
        )
    }

    @Provides
    @Singleton
    fun provideUserUseCase(
        userRepository: UserRepository,
        localDatabase: LocalDatabase
    ): UserUseCase {
        return UserUseCase(userRepository, localDatabase)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
