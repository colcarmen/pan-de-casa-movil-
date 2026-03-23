package com.pan_de_casa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pan_de_casa.data.local.dao.OrderDao
import com.pan_de_casa.data.local.dao.ProductDao
import com.pan_de_casa.data.local.entity.OrderEntity
import com.pan_de_casa.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, OrderEntity::class],
    version = 2, // Incrementamos de 1 a 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
}
