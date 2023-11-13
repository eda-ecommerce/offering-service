package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.data.models.TestEntity

@ApplicationScoped
class TestEntityConsumer {
    @Incoming("test-entity-in")
    fun consume(value: TestEntity) {
        println(value)
    }

}
