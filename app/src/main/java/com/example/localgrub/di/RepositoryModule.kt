package com.example.localgrub.di

import com.example.localgrub.data.remote.repository.AuthRepositoryImpl
import com.example.localgrub.data.remote.repository.DishesRepositoryImpl
import com.example.localgrub.data.remote.repository.OrderRepositoryImpl
import com.example.localgrub.data.remote.repository.OwnerRepositoryImpl
import com.example.localgrub.data.remote.repository.TokenRepositoryImpl
import com.example.localgrub.data.remote.repository.UserRepositoryImpl
import com.example.localgrub.domain.repository.AuthRepository
import com.example.localgrub.domain.repository.DishesRepository
import com.example.localgrub.domain.repository.OrderRepository
import com.example.localgrub.domain.repository.OwnerRepository
import com.example.localgrub.domain.repository.TokenRepository
import com.example.localgrub.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindDishesRepository(impl: DishesRepositoryImpl): DishesRepository

    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    abstract fun bindOwnerRepository(impl: OwnerRepositoryImpl): OwnerRepository

    @Binds
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository
}