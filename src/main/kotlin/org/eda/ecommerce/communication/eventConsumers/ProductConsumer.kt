package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.data.models.events.ProductEvent
import org.eda.ecommerce.services.ProductService

@ApplicationScoped
class ProductConsumer {

    @Inject
    private lateinit var productService: ProductService

    @Incoming("product-in")
    @Transactional
    fun consume(productEvent: ProductEvent) {
        println("Creating Product: " + productEvent.content)
        productService.createNewProduct(productEvent.content)
    }

}
