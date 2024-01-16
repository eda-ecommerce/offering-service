package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.repositories.ProductRepository
import java.util.*

@ApplicationScoped
class ProductService {

    @Inject
    private lateinit var productRepository: ProductRepository

    fun getAll(): List<Product> {
        return productRepository.listAll()
    }

    fun findById(id: UUID): Product {
        return productRepository.findById(id)
    }

    fun createNewProduct(product: Product) {
        println("Creating Product: $product")
        productRepository.persist(product)
    }

    fun updateProduct(product: Product) {
        val entity = productRepository.findById(product.id) ?: return

        entity.apply {
            status = product.status
        }

    }
}
