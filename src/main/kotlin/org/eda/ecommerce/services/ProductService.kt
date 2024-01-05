package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.repositories.ProductRepository

@ApplicationScoped
class ProductService {

    @Inject
    private lateinit var productRepository: ProductRepository

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
