package com.example.localgrub.di

import com.example.localgrub.data.local.LocalDatabase
import com.example.localgrub.domain.repository.AuthRepository
import com.example.localgrub.domain.repository.DishesRepository
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.domain.repository.TokenRepository
import com.example.localgrub.domain.repository.UserRepository
import com.example.localgrub.domain.usecase.AuthUseCase
import com.example.localgrub.domain.usecase.DishesUseCase
import com.example.localgrub.domain.usecase.OrderUseCase
import com.example.localgrub.domain.usecase.TokenUseCase
import com.example.localgrub.domain.usecase.UserUseCase
import com.example.localgrub.workerscheduler.SenderNotificationWorkerScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler,
        tokenRepository: TokenRepository,
        notificationRepository: NotificationRepository
    ): OrderUseCase {
        return OrderUseCase(
            orderRepository,
            senderNotificationWorkerScheduler = senderNotificationWorkerScheduler,
            tokenRepository = tokenRepository,
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
    fun provideTokenUseCase(
        tokenRepository: TokenRepository
    ): TokenUseCase {
        return TokenUseCase(tokenRepository)
    }
}
