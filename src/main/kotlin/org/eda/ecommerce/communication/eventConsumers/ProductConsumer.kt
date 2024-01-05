package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.services.ProductService

@ApplicationScoped
class ProductConsumer {

    @Inject
    private lateinit var productService: ProductService

    @Incoming("product-in")
    @Transactional
    fun consume(record: ConsumerRecord<String, Product>) {
        val operation = record.headers().lastHeader("operation")
        println("Product event operation: ${String(operation.value())}")

        when (String(operation.value())) {
            "created" -> productService.createNewProduct(record.value())
            "updated" -> productService.updateProduct(record.value())
        }
    }

}
