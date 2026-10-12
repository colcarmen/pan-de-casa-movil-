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
            android.util.Log.d("SupabaseDebug", "Conectando a Supabase para obtener catálogo...")
            val remoteProducts = postgrest.from("products")
                .select()
                .decodeList<ProductDto>()
            
            android.util.Log.d("SupabaseDebug", "Productos obtenidos de Supabase con éxito: ${remoteProducts.size}")
            val domainProducts = remoteProducts.map { it.toDomain() }
            
            productDao.deleteAllProducts()
            productDao.insertProducts(domainProducts.map { ProductEntity.fromDomain(it) })
            
            emit(domainProducts)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", ">>> ERROR DE CONEXIÓN A SUPABASE: ${e.javaClass.simpleName} - ${e.message} <<<", e)
            emitAll(productDao.getAllProducts().map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return try {
            val productId = id.toLongOrNull() ?: return null
            val dto = postgrest.from("products")
                .select {
                    filter {
                        eq("id", productId)
                    }
                }
                .decodeSingleOrNull<ProductDto>()
            dto?.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "Error getProductById($id): ${e.message}", e)
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
            android.util.Log.e("SupabaseError", "Error addProduct: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val productId = product.id.toLongOrNull() ?: return Result.failure(Exception("ID inválido"))
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
            android.util.Log.e("SupabaseError", "Error updateProduct: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val productId = id.toLongOrNull()
            if (productId != null) {
                postgrest.from("products").delete {
                    filter {
                        eq("id", productId)
                    }
                }
            }
            // También eliminamos de la base de datos local para mantener sincronía
            productDao.deleteProduct(id)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "Error deleteProduct: ${e.message}", e)
            // Aún si falla en remoto, limpiamos localmente
            productDao.deleteProduct(id)
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
