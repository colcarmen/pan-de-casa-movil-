package com.pan_de_casa.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pan_de_casa.data.local.dao.ProductDao
import com.pan_de_casa.data.local.entity.ProductEntity
import com.pan_de_casa.data.remote.dto.ProductDto
import com.pan_de_casa.domain.model.Product
import com.pan_de_casa.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProductRepository {

    override fun getProducts(): Flow<List<Product>> = flow {
        try {
            android.util.Log.d("FirebaseDebug", "Conectando a Firebase Firestore para obtener catálogo...")
            val snapshot = firestore.collection("products").get().await()
            var remoteProducts = snapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(ProductDto::class.java)
                dto?.copy(id = doc.id)?.toDomain()
            }
            android.util.Log.d("FirebaseDebug", "Productos obtenidos de Firebase: ${remoteProducts.size}")

            // Si Firestore está vacío (recién inicializado), sembramos automáticamente el catálogo inicial
            if (remoteProducts.isEmpty()) {
                android.util.Log.d("FirebaseDebug", "Colección 'products' vacía en Firestore. Sembrando catálogo inicial...")
                remoteProducts = seedInitialProducts()
            }

            if (remoteProducts.isNotEmpty()) {
                productDao.deleteAllProducts()
                productDao.insertProducts(remoteProducts.map { ProductEntity.fromDomain(it) })
                emit(remoteProducts)
            } else {
                emitAll(productDao.getAllProducts().map { entities ->
                    entities.map { it.toDomain() }
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error al obtener productos de Firebase: ${e.message}", e)
            emitAll(productDao.getAllProducts().map { entities ->
                entities.map { it.toDomain() }
            })
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return try {
            val doc = firestore.collection("products").document(id).get().await()
            val dto = doc.toObject(ProductDto::class.java)
            dto?.copy(id = doc.id)?.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error getProductById($id): ${e.message}", e)
            productDao.getProductById(id)?.toDomain()
        }
    }

    override suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "imageUrl" to product.imageUrl,
                "category" to product.category,
                "createdAt" to System.currentTimeMillis()
            )
            val docRef = firestore.collection("products").add(data).await()
            val savedProduct = product.copy(id = docRef.id)
            productDao.insertProducts(listOf(ProductEntity.fromDomain(savedProduct)))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error addProduct: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val data = hashMapOf<String, Any>(
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "imageUrl" to product.imageUrl,
                "category" to product.category
            )
            firestore.collection("products").document(product.id).update(data).await()
            productDao.insertProducts(listOf(ProductEntity.fromDomain(product)))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error updateProduct: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            firestore.collection("products").document(id).delete().await()
            productDao.deleteProduct(id)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseError", "Error deleteProduct: ${e.message}", e)
            productDao.deleteProduct(id)
            Result.failure(e)
        }
    }

    override suspend fun uploadProductImage(bytes: ByteArray, fileName: String): Result<String> {
        return try {
            val ref = storage.reference.child("product-images/$fileName")
            ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedInitialProducts(): List<Product> {
        val initialList = listOf(
            Product(
                name = "Pan de Masa Madre Tradicional",
                description = "Pan rústico de fermentación lenta (24h) con corteza crujiente y miga aireada.",
                price = 12000.0,
                imageUrl = "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?auto=format&fit=crop&w=600&q=80",
                category = "Artesanal"
            ),
            Product(
                name = "Pan de Centeno y Semillas",
                description = "Elaborado con harina integral de centeno, semillas de girasol, lino y chía.",
                price = 14000.0,
                imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80",
                category = "Integral"
            ),
            Product(
                name = "Croissant de Mantequilla",
                description = "Clásico croissant francés hojaldrado con mantequilla 100% natural de primera calidad.",
                price = 6500.0,
                imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80",
                category = "Repostería"
            ),
            Product(
                name = "Baguette Rústica",
                description = "Baguette tradicional francesa, corteza dorada y crujiente, ideal para acompañar tus comidas.",
                price = 5000.0,
                imageUrl = "https://images.unsplash.com/photo-1597079910443-60c43fc4f749?auto=format&fit=crop&w=600&q=80",
                category = "Artesanal"
            ),
            Product(
                name = "Pan de Queso y Ajo",
                description = "Pan suave horneado relleno con queso mozzarella fundido y mantequilla de ajo con finas hierbas.",
                price = 8500.0,
                imageUrl = "https://images.unsplash.com/photo-1619535860434-ba1d8fa12536?auto=format&fit=crop&w=600&q=80",
                category = "Especialidades"
            ),
            Product(
                name = "Chocolatín (Pain au Chocolat)",
                description = "Hojaldre suave relleno de dos barras de chocolate belga semi-amargo.",
                price = 7000.0,
                imageUrl = "https://images.unsplash.com/photo-1608198093002-ad4e005484ec?auto=format&fit=crop&w=600&q=80",
                category = "Repostería"
            )
        )

        val createdProducts = mutableListOf<Product>()
        for (item in initialList) {
            try {
                val data = hashMapOf(
                    "name" to item.name,
                    "description" to item.description,
                    "price" to item.price,
                    "imageUrl" to item.imageUrl,
                    "category" to item.category,
                    "createdAt" to System.currentTimeMillis()
                )
                val docRef = firestore.collection("products").add(data).await()
                createdProducts.add(item.copy(id = docRef.id))
            } catch (e: Exception) {
                android.util.Log.e("FirebaseError", "Error al sembrar producto ${item.name}: ${e.message}")
            }
        }
        return if (createdProducts.isNotEmpty()) createdProducts else initialList
    }

    private fun ProductDto.toDomain(): Product {
        return Product(
            id = id ?: "",
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl ?: "",
            category = category ?: "General"
        )
    }
}
