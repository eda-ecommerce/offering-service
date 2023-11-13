package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.models.events.ProductEvent
import org.eda.ecommerce.data.repositories.ProductRepository

@ApplicationScoped
class ProductService {

    @Inject
    private lateinit var productRepository: ProductRepository

    @Inject
    @Channel("product-out")
    private lateinit var productEmitter: Emitter<ProductEvent>

    fun getAll(): List<Product> {
        return productRepository.listAll()
    }

    fun findById(id: Long): Product {
        return productRepository.findById(id)
    }

    fun createNewProduct(product: Product) {
        productRepository.persistWithTransaction(product)

        val productEvent = ProductEvent(
            source = "product-service",
            type = "created",
            payload = product
        )

        productEmitter.send(productEvent)
    }

}
