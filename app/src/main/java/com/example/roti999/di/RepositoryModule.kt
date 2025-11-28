package com.example.roti999.di

import com.example.roti999.data.repository.AuthRepositoryImpl
import com.example.roti999.data.repository.DishesRepositoryImpl
import com.example.roti999.data.repository.OrderRepositoryImpl
import com.example.roti999.data.repository.UserRepositoryImpl
import com.example.roti999.domain.repository.AuthRepository
import com.example.roti999.domain.repository.DishesRepository
import com.example.roti999.domain.repository.OrderRepository
import com.example.roti999.domain.repository.UserRepository
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
}