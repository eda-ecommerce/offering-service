package org.eda.ecommerce.data.models.events

import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata
import org.eda.ecommerce.data.models.Product

class ProductEvent(operation: String, var product: Product): Message<Product> {
    override fun getPayload(): Product = product
    override fun getMetadata(): Metadata = Metadata.empty()
}
