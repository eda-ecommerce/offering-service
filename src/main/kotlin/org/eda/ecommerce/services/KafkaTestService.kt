package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Message
import java.util.concurrent.CompletableFuture

@ApplicationScoped
class KafkaTestService {
    @Inject
    @Channel("test-out")
    private lateinit var testEmitter: Emitter<String>

    fun emitTestMessage(message: String) {
        testEmitter.send(
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
