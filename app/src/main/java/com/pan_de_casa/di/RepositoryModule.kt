package com.pan_de_casa.di

import com.pan_de_casa.data.repository.AuthRepositoryImpl
import com.pan_de_casa.data.repository.CartRepositoryImpl
import com.pan_de_casa.data.repository.OrderRepositoryImpl
import com.pan_de_casa.data.repository.ProductRepositoryImpl
import com.pan_de_casa.domain.repository.AuthRepository
import com.pan_de_casa.domain.repository.CartRepository
import com.pan_de_casa.domain.repository.OrderRepository
import com.pan_de_casa.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository
}
