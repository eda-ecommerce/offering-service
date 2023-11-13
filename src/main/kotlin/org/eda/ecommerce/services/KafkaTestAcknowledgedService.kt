package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Message
import java.util.concurrent.CompletableFuture

@ApplicationScoped
class KafkaTestAcknowledgedService {
    @Inject
    @Channel("test-acknowledged-out")
    private lateinit var testAcknowledgedEmitter: Emitter<String>

    fun emitTestMessage(message: String) {
        testAcknowledgedEmitter.send(
            Message.of(message)
                .withAck {
                    println("Acked")
                    CompletableFuture.completedFuture(
                        null
                    )
                }
                .withNack { throwable: Throwable? ->
                    println("Nacked")
                    CompletableFuture.completedFuture(
                        null
                    )
                })
    }
}
