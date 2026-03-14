package com.example.localgrub.di

import com.example.localgrub.data.local.LocalDatabase
import com.example.localgrub.domain.repository.DishesRepository
import com.example.localgrub.domain.repository.LoginRepository
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.OfferRepository
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.domain.repository.OtpRepository
import com.example.localgrub.domain.repository.UserRepository
import com.example.localgrub.domain.usecase.DishesUseCase
import com.example.localgrub.domain.usecase.LoginUseCase
import com.example.localgrub.domain.usecase.NotificationUseCase
import com.example.localgrub.domain.usecase.OfferUseCase
import com.example.localgrub.domain.usecase.OrderUseCase
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
    fun provideDishesUseCase(dishesRepository: DishesRepository): DishesUseCase {
        return DishesUseCase(dishesRepository)
    }

    @Provides
    @Singleton
    fun provideOrderUseCase(
        orderRepository: OrderRepository,
        senderNotificationWorkerScheduler: SenderNotificationWorkerScheduler,
        notificationRepository: NotificationRepository
    ): OrderUseCase {
        return OrderUseCase(
            orderRepository,
            senderNotificationWorkerScheduler = senderNotificationWorkerScheduler,
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
    fun provideOfferUseCase(
        offerRepository: OfferRepository
    ): OfferUseCase {
        return OfferUseCase(offerRepository)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(
        loginRepository: LoginRepository,
        localDatabase: LocalDatabase,
        notificationRepository: NotificationRepository,
        otpRepository: OtpRepository
    ): LoginUseCase {
        return LoginUseCase(
            loginRepository = loginRepository,
            localDatabase = localDatabase,
            notificationRepository = notificationRepository,
            otpRepository = otpRepository
        )
    }

    @Provides
    @Singleton
    fun provideNotificationUseCase(
        notificationRepository: NotificationRepository
    ): NotificationUseCase {
        return NotificationUseCase(notificationRepository)
    }
}