package com.example.localgrub.di

import com.example.localgrub.data.remote.repository.DishesRepositoryImpl
import com.example.localgrub.data.remote.repository.LoginRepositoryImpl
import com.example.localgrub.data.remote.repository.NotificationRepositoryImpl
import com.example.localgrub.data.remote.repository.OfferRepositoryImpl
import com.example.localgrub.data.remote.repository.OrderRepositoryImpl
import com.example.localgrub.data.remote.repository.OtpRepositoryImpl
import com.example.localgrub.data.remote.repository.UserRepositoryImpl
import com.example.localgrub.domain.repository.DishesRepository
import com.example.localgrub.domain.repository.LoginRepository
import com.example.localgrub.domain.repository.NotificationRepository
import com.example.localgrub.domain.repository.OfferRepository
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.domain.repository.OtpRepository
import com.example.localgrub.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindDishesRepository(impl: DishesRepositoryImpl): DishesRepository

    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    abstract fun bindOfferRepository(impl: OfferRepositoryImpl): OfferRepository

    @Binds
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindOtpRepository(impl: OtpRepositoryImpl): OtpRepository
}