package com.pan_de_casa.data.repository

import com.pan_de_casa.data.local.dao.ProductDao
import com.pan_de_casa.data.local.entity.ProductEntity
import com.pan_de_casa.data.remote.dto.ProductDto
import com.pan_de_casa.domain.model.Product
import com.pan_de_casa.domain.repository.ProductRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val postgrest: Postgrest,
    private val storage: Storage
) : ProductRepository {

    override fun getProducts(): Flow<List<Product>> = flow {
        try {
            val remoteProducts = postgrest.from("products")
                .select()
                .decodeList<ProductDto>()
            
            val domainProducts = remoteProducts.map { it.toDomain() }
            
            productDao.deleteAllProducts()
            productDao.insertProducts(domainProducts.map { ProductEntity.fromDomain(it) })
            
            emit(domainProducts)
        } catch (e: Exception) {
            emitAll(productDao.getAllProducts().map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return try {
            val productId = id.toIntOrNull() ?: return null
            val dto = postgrest.from("products")
                .select {
                    filter {
                        eq("id", productId)
                    }
                }
                .decodeSingleOrNull<ProductDto>()
            dto?.toDomain()
        } catch (e: Exception) {
            productDao.getProductById(id)?.toDomain()
        }
    }

    override suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val dto = ProductDto(
                name = product.name,
                description = product.description,
                price = product.price,
                image_url = product.imageUrl,
                category = product.category
            )
            postgrest.from("products").insert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val productId = product.id.toIntOrNull() ?: return Result.failure(Exception("ID inválido"))
            val dto = ProductDto(
                name = product.name,
                description = product.description,
                price = product.price,
                image_url = product.imageUrl,
                category = product.category
            )
            postgrest.from("products").update(dto) {
                filter {
                    eq("id", productId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val productId = id.toIntOrNull() ?: return Result.failure(Exception("ID inválido"))
            postgrest.from("products").delete {
                filter {
                    eq("id", productId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProductImage(bytes: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = storage.from("product-images")
            bucket.upload(path = fileName, data = bytes, upsert = true)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProductDto.toDomain(): Product {
        return Product(
            id = id?.toString() ?: "",
            name = name,
            description = description,
            price = price,
            imageUrl = image_url ?: "",
            category = category ?: "General"
        )
    }
}
