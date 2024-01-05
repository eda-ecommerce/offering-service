package org.eda.ecommerce.data.models.events

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata
import org.apache.kafka.common.header.internals.RecordHeaders
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata
import org.eda.ecommerce.data.models.Offering
import org.eda.ecommerce.data.models.Product

open class OfferingEvent(operation: String, offering: Offering) : Message<Offering> {
    private val message: Message<Offering> = createMessageWithMetadata(offering, operation)

    override fun getPayload(): Offering = message.payload
    override fun getMetadata(): Metadata = message.metadata
    companion object {
        private fun createMessageWithMetadata(offering: Offering, operation: String): Message<Offering> {
            val metadata = Metadata.of(
                OutgoingKafkaRecordMetadata.builder<String>()
                    .withHeaders(RecordHeaders().apply {
                        add("operation", operation.toByteArray())
                        add("source", "offering".toByteArray())
                        add("timestamp", System.currentTimeMillis().toString().toByteArray())
                    }).build()
            )
            return Message.of(offering, metadata)
        }
    }
}

class OfferingCreatedEvent(offering: Offering) : OfferingEvent("created", offering)

class OfferingUpdatedEvent(offering: Offering) : OfferingEvent("updated", offering)

class OfferingDeletedEvent(offering: Offering) : OfferingEvent("deleted", offering)
