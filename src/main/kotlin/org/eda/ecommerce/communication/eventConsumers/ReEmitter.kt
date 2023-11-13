package org.eda.ecommerce.communication.eventConsumers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eda.ecommerce.services.KafkaTestAcknowledgedService


@ApplicationScoped
class ReEmitter {

    @Inject
    private lateinit var kafkaTestAcknowledgedService: KafkaTestAcknowledgedService

    @Incoming("test-in")
    // Re-emits any events received via the `test-in` channel (kafka topic 'test') to the `test-acknowledged-out` channel (kafka topic 'test-acknowledged').
    fun consume(value: String) {
        println(value)

        kafkaTestAcknowledgedService.emitTestMessage(value)
    }
}
