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
        productRepository.persist(product)
    }

}
