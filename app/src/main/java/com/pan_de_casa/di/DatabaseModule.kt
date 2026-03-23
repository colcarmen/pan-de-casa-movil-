package com.pan_de_casa.di

import android.content.Context
import androidx.room.Room
import com.pan_de_casa.data.local.LocalDatabase
import com.pan_de_casa.data.local.dao.OrderDao
import com.pan_de_casa.data.local.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "pan_de_casa_db"
        )
        .fallbackToDestructiveMigration() // Agregamos esto para evitar cierres por cambios de esquema
        .build()
    }

    @Provides
    fun provideProductDao(db: LocalDatabase): ProductDao = db.productDao()

    @Provides
    fun provideOrderDao(db: LocalDatabase): OrderDao = db.orderDao()
}
